// 2019. Mahesh Voleti (mvoleti@vmware.com)

package delivery

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/go-chi/chi"

	. "wavefront.com/hackathon/beachshirts"
)

type DeliveryServer struct {
	HostURL       string
	Router        *chi.Mux
	DeliveryQueue chan PackedShirts
}

func NewServer() *DeliveryServer {
	r := chi.NewRouter()
	deliveryQueue := make(chan PackedShirts, GlobalConfig.DeliveryQueueSize)

	server := &DeliveryServer{GlobalConfig.DeliveryHost, r, deliveryQueue}

	r.Route("/delivery", func(r chi.Router) {
		r.Post("/dispatch/{orderNum}", server.dispatch)
		r.Get("/return/{orderNum}", server.retrieve)
	})

	return server
}

func (s *DeliveryServer) Start() error {
	log.Println("Delivery Server listening @", s.HostURL)

	ticker := time.NewTicker(30 * time.Second)
	go func() {
		for _ = range ticker.C {
			log.Printf("Processing %d entries in the dispatch queue!\n", len(s.DeliveryQueue))
			done := false
			for !done {
				select {
				case item := <-s.DeliveryQueue:
					log.Printf("\t%d shirts delivered\n", len(item.Shirts))
				default:
					done = true
				}
			}
		}
	}()

	return http.ListenAndServe(s.HostURL, s.Router)
}

func (s *DeliveryServer) dispatch(w http.ResponseWriter, r *http.Request) {

	time.Sleep(time.Duration(RAND.Intn(GlobalConfig.SimDelayDelivery)) * time.Second)

	if RAND.Float32() < GlobalConfig.SimFailDelivery1 {
		WriteError(w, "Failed to dispatch shirts!", http.StatusServiceUnavailable)
		return
	}

	orderNum := chi.URLParam(r, "orderNum")

	if RAND.Float32() < GlobalConfig.SimFailDelivery2 {
		orderNum = ""
	}

	if orderNum == "" {
		WriteError(w, "Invalid Order Num", http.StatusBadRequest)
		return
	}

	var packedShirts PackedShirts
	json.NewDecoder(r.Body).Decode(&packedShirts)

	if RAND.Float32() < GlobalConfig.SimFailDelivery3 {
		packedShirts = PackedShirts{}
	}

	if packedShirts.Shirts == nil || len(packedShirts.Shirts) == 0 {
		WriteError(w, "No shirts to deliver", http.StatusBadRequest)
		return
	}

	s.DeliveryQueue <- packedShirts

	trackingNum := NewOrderNum()
	log.Printf("Tracking number of Order: %s is %s\n", orderNum, trackingNum)

	status, _ := json.Marshal(DeliveryStatus{OrderNum: orderNum, TrackingNum: trackingNum, Status: "shirts delivery dispatched"})
	w.Write(status)
}

func (s *DeliveryServer) retrieve(w http.ResponseWriter, r *http.Request) {
	orderNum := chi.URLParam(r, "orderNum")
	if orderNum == "" {
		WriteError(w, "Invalid Order Num", http.StatusBadRequest)
	}
	fmt.Fprintf(w, "Order: %s returned\n", orderNum)
}
