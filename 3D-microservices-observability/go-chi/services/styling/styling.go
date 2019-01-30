// 2019. Mahesh Voleti (mvoleti@vmware.com)

package styling

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"strconv"

	"github.com/go-chi/chi"

	. "wavefront.com/hackathon/beachshirts/internal"
)

type StylingServer struct {
	HostURL string
	Router  *chi.Mux

	Styles []ShirtStyle
}

func NewServer() Server {
	r := chi.NewRouter()
	styles := []ShirtStyle{{Name: "beachops", ImageUrl: "beachopsImage"},
		{Name: "style1", ImageUrl: "style1Image"},
		{Name: "style2", ImageUrl: "style2Image"},
	}

	server := &StylingServer{GlobalConfig.StylingHost, r, styles}

	// Routes
	r.Route("/style", func(r chi.Router) {
		r.Get("/", server.getStyles)
		r.Get("/{id}/make", server.makeShirts)
	})

	return server
}

func (s *StylingServer) Start() error {
	log.Println("Styling Server listening @", s.HostURL)
	return http.ListenAndServe(s.HostURL, s.Router)
}

func (s *StylingServer) getStyles(w http.ResponseWriter, r *http.Request) {

	RandSimDelay()

	out, _ := json.Marshal(s.Styles)
	w.Write(out)
}

func (s *StylingServer) makeShirts(w http.ResponseWriter, r *http.Request) {

	RandSimDelay()

	if RAND.Float32() < GlobalConfig.SimFailStyling {
		WriteError(w, "Failed to make shirts!", http.StatusServiceUnavailable)
		return
	}

	id := chi.URLParam(r, "id")
	quantity, parserr := strconv.Atoi(r.URL.Query().Get("quantity"))
	if parserr != nil {
		WriteError(w, "Parsing error for quantity!", http.StatusInternalServerError)
		return
	}

	orderNum := NewOrderNum()

	packedShirts := PackedShirts{Shirts: make([]Shirt, quantity)}
	for i := 0; i < quantity; i += 1 {
		name := "style" + id
		packedShirts.Shirts[i] = Shirt{Style: ShirtStyle{Name: name, ImageUrl: name + "Image"}}
	}

	resp, err := callDeliveryDispatch(orderNum, packedShirts)
	if err != nil {
		WriteError(w, "Failed to make shirts!", http.StatusPreconditionFailed)
		return
	}
	defer resp.Body.Close()

	RandSimDelay()

	if resp.StatusCode == http.StatusOK {
		io.Copy(w, resp.Body)
	} else {
		WriteError(w, fmt.Sprintf("Failed to make shirts! (%s)", resp.Status), resp.StatusCode)
	}
}

func callDeliveryDispatch(orderNum string, packedShirts PackedShirts) (*http.Response, error) {
	deliveryURL := fmt.Sprintf("http://%s/delivery/dispatch/%s", GlobalConfig.DeliveryHost, orderNum)
	dispatchBody, _ := json.Marshal(packedShirts)
	return POSTCall(deliveryURL, "application/json", bytes.NewReader(dispatchBody))
}
