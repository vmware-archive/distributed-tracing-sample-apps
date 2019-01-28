// 2019. Mahesh Voleti (mvoleti@vmware.com)

package delivery

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/go-chi/chi"
	opentracing "github.com/opentracing/opentracing-go"
	otrext "github.com/opentracing/opentracing-go/ext"

	. "wavefront.com/hackathon/beachshirts"
)

type DeliveryServer struct {
	HostURL       string
	Router        *chi.Mux
	DeliveryQueue chan PackedShirts

	tracer opentracing.Tracer
}

func NewServer() *DeliveryServer {
	r := chi.NewRouter()
	deliveryQueue := make(chan PackedShirts, GlobalConfig.DeliveryQueueSize)

	server := &DeliveryServer{GlobalConfig.DeliveryHost, r, deliveryQueue, opentracing.GlobalTracer()}

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
			span := s.tracer.StartSpan("processDispatch")

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

			span.Finish()
		}
	}()

	return http.ListenAndServe(s.HostURL, s.Router)
}

func (s *DeliveryServer) dispatch(w http.ResponseWriter, r *http.Request) {
	span := NewServerSpan(r, "dispatch")
	defer span.Finish()

	RandSimDelay()

	if RAND.Float32() < GlobalConfig.SimFailDelivery1 {
		otrext.Error.Set(span, true)
		WriteError(w, "Failed to dispatch shirts!", http.StatusServiceUnavailable)
		return
	}

	orderNum := chi.URLParam(r, "orderNum")

	if RAND.Float32() < GlobalConfig.SimFailDelivery2 {
		orderNum = ""
	}

	if orderNum == "" {
		otrext.Error.Set(span, true)
		WriteError(w, "Invalid Order Num", http.StatusBadRequest)
		return
	}

	var packedShirts PackedShirts
	json.NewDecoder(r.Body).Decode(&packedShirts)

	if RAND.Float32() < GlobalConfig.SimFailDelivery3 {
		packedShirts = PackedShirts{}
	}

	if packedShirts.Shirts == nil || len(packedShirts.Shirts) == 0 {
		otrext.Error.Set(span, true)
		WriteError(w, "No shirts to deliver", http.StatusBadRequest)
		return
	}

	s.DeliveryQueue <- packedShirts

	trackingNum := NewOrderNum()
	log.Printf("Tracking number of Order: %s is %s\n", orderNum, trackingNum)

	RandSimDelay()

	status, _ := json.Marshal(DeliveryStatus{OrderNum: orderNum, TrackingNum: trackingNum, Status: "shirts delivery dispatched"})
	w.Write(status)
}

func (s *DeliveryServer) retrieve(w http.ResponseWriter, r *http.Request) {
	span := NewServerSpan(r, "retrieve")
	defer span.Finish()

	orderNum := chi.URLParam(r, "orderNum")
	if orderNum == "" {
		otrext.Error.Set(span, true)
		WriteError(w, "Invalid Order Num", http.StatusBadRequest)
	}
	fmt.Fprintf(w, "Order: %s returned\n", orderNum)
}
