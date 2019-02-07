// 2019. Mahesh Voleti (mvoleti@vmware.com)

package shopping

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"

	opentracing "github.com/opentracing/opentracing-go"
	otrext "github.com/opentracing/opentracing-go/ext"

	"github.com/go-chi/chi"

	. "wavefront.com/hackathon/beachshirts/internal"
)

type ShoppingServer struct {
	HostURL string
	Router  *chi.Mux

	tracer opentracing.Tracer
}

func NewServer() Server {
	r := chi.NewRouter()

	server := &ShoppingServer{GlobalConfig.ShoppingHost, r, opentracing.GlobalTracer()}

	r.Route("/shop", func(r chi.Router) {
		r.Get("/menu", server.getShoppingMenu)
		r.Post("/order", server.orderShirts)
	})

	return server
}

func (s *ShoppingServer) Start() error {
	log.Println("Shopping Server listening @", s.HostURL)
	return http.ListenAndServe(s.HostURL, s.Router)
}

func (s *ShoppingServer) getShoppingMenu(w http.ResponseWriter, r *http.Request) {
	span := s.tracer.StartSpan("getShoppingMenu")
	defer span.Finish()

	resp, err := callGetAllStyles(span.Context())
	if err != nil {
		otrext.Error.Set(span, true)
		WriteError(w, "Failed to get menu!", http.StatusPreconditionFailed)
	}
	defer resp.Body.Close()

	io.Copy(w, resp.Body)
}

func (s *ShoppingServer) orderShirts(w http.ResponseWriter, r *http.Request) {
	span := s.tracer.StartSpan("orderShirts")
	defer span.Finish()

	RandSimDelay()

	var order Order
	decerr := json.NewDecoder(r.Body).Decode(&order)
	if decerr != nil {
		WriteError(w, "Invalid Request to order shirts!", http.StatusBadRequest)
		return
	}

	if RAND.Float32() < GlobalConfig.SimFailShopping {
		otrext.Error.Set(span, true)
		WriteError(w, "Failed to order shirts!", http.StatusServiceUnavailable)
		return
	}

	resp, err := callMakeShirts(order, span.Context())
	if err != nil {
		otrext.Error.Set(span, true)
		WriteError(w, err.Error(), http.StatusPreconditionFailed)
		return
	}
	defer resp.Body.Close()

	RandSimDelay()

	if resp.StatusCode == http.StatusOK {
		io.Copy(w, resp.Body)
	} else {
		otrext.Error.Set(span, true)
		WriteError(w, fmt.Sprintf("Failed to order shirts! (%s)", resp.Status), resp.StatusCode)
	}

}

func callGetAllStyles(spanCtx opentracing.SpanContext) (*http.Response, error) {
	stylingURL := fmt.Sprintf("http://%s/style/", GlobalConfig.StylingHost)
	return GETCall(stylingURL, nil, spanCtx)
}

func callMakeShirts(order Order, spanCtx opentracing.SpanContext) (*http.Response, error) {
	stylingURL := fmt.Sprintf("http://%s/style/%s/make?quantity=%d", GlobalConfig.StylingHost, order.StyleName, order.Quantity)
	return GETCall(stylingURL, nil, spanCtx)
}
