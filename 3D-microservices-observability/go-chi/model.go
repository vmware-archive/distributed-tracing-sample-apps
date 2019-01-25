// 2019. Mahesh Voleti (mvoleti@vmware.com)

package beachshirts

type BeachShirtsConfig struct {
	Service      string
	ShoppingHost string
	StylingHost  string
	DeliveryHost string

	DeliveryQueueSize int

	// simulation
	SimFailShopping  float32
	SimFailStyling   float32
	SimFailDelivery1 float32
	SimFailDelivery2 float32
	SimFailDelivery3 float32
	SimDelayDelivery int // seconds
}

var GlobalConfig BeachShirtsConfig

func InitGlobalConfig() {
	GlobalConfig = BeachShirtsConfig{
		Service:      "",
		ShoppingHost: "localhost:50050",
		StylingHost:  "localhost:50051",
		DeliveryHost: "localhost:50052",

		DeliveryQueueSize: 100,

		SimFailShopping:  0.1,
		SimFailStyling:   0.2,
		SimFailDelivery1: 0.2,
		SimFailDelivery2: 0.1,
		SimFailDelivery3: 0.1,
		SimDelayDelivery: 5,
	}
}

type Server interface {
	Start() error
}

type ErrorStatus struct {
	Error string `json:"error"`
}

// Shopping
type Order struct {
	StyleName string
	Quantity  int
}

type OrderStatus struct {
	OrderId string
	Status  string
}

// Styling
type PackedShirts struct {
	Shirts []Shirt
}

type Shirt struct {
	Style ShirtStyle
}

type ShirtStyle struct {
	Name     string
	ImageUrl string
}

// Delivery
type DeliveryStatus struct {
	OrderNum    string
	TrackingNum string
	Status      string
}
