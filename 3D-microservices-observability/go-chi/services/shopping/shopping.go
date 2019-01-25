// 2019. Mahesh Voleti (mvoleti@vmware.com)

package shopping

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"

	"github.com/go-chi/chi"

	. "wavefront.com/hackathon/beachshirts"
)

type ShoppingServer struct {
	HostURL string
	Router  *chi.Mux
}

func NewServer() *ShoppingServer {
	r := chi.NewRouter()

	server := &ShoppingServer{GlobalConfig.ShoppingHost, r}

	r.Route("/shop", func(r chi.Router) {
		r.Get("/menu", server.getMenu)
		r.Post("/order", server.orderShirts)
	})

	return server
}

func (s *ShoppingServer) Start() error {
	log.Println("Shopping Server listening @", s.HostURL)
	return http.ListenAndServe(s.HostURL, s.Router)
}

func (s *ShoppingServer) getMenu(w http.ResponseWriter, r *http.Request) {
	resp, err := callGetAllStyles()
	if err != nil {
		WriteError(w, "Failed to get menu!", http.StatusPreconditionFailed)
	}
	io.Copy(w, resp.Body)
}

func (s *ShoppingServer) orderShirts(w http.ResponseWriter, r *http.Request) {
	var order Order
	json.NewDecoder(r.Body).Decode(&order)

	if RAND.Float32() < GlobalConfig.SimFailShopping {
		WriteError(w, "Failed to order shirts!", http.StatusServiceUnavailable)
		return
	}

	resp, err := callMakeShirts(order)
	if err != nil {
		WriteError(w, err.Error(), http.StatusPreconditionFailed)
		return
	}
	if resp.StatusCode == http.StatusOK {
		io.Copy(w, resp.Body)
	} else {
		WriteError(w, fmt.Sprintf("Failed to order shirts! (%s)", resp.Status), resp.StatusCode)
	}
}

func callGetAllStyles() (*http.Response, error) {
	stylingURL := fmt.Sprintf("http://%s/style/", GlobalConfig.StylingHost)
	return http.Get(stylingURL)
}

func callMakeShirts(order Order) (*http.Response, error) {
	stylingURL := fmt.Sprintf("http://%s/style/%s/make?quantity=%d", GlobalConfig.StylingHost, order.StyleName, order.Quantity)
	return http.Get(stylingURL)
}
