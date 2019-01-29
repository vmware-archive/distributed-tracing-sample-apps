// 2019. Mahesh Voleti (mvoleti@vmware.com)

package internal

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
	SimDelayChance   float32
	SimDelayMS       int // milliseconds
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
		SimDelayChance:   0.3333,
		SimDelayMS:       1000,
	}
}
