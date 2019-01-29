package main

import (
	"encoding/json"
	"fmt"
	"os"

	. "wavefront.com/hackathon/beachshirts/internal"
	"wavefront.com/hackathon/beachshirts/services/delivery"
	"wavefront.com/hackathon/beachshirts/services/shopping"
	"wavefront.com/hackathon/beachshirts/services/styling"
)

func main() {
	if len(os.Args) < 2 {
		fmt.Printf("Usage: beachshirts <config_file>\n")
		os.Exit(1)
	}

	InitGlobalConfig()

	file, ferr := os.Open(os.Args[1])
	if ferr != nil {
		fmt.Println(ferr)
		os.Exit(2)
	}

	if derr := json.NewDecoder(file).Decode(&GlobalConfig); derr != nil {
		fmt.Println(derr)
		os.Exit(2)
	}

	var server Server

	switch GlobalConfig.Service {
	case "shopping":
		server = shopping.NewServer()

	case "styling":
		server = styling.NewServer()

	case "delivery":
		server = delivery.NewServer()

	default:
		fmt.Printf("Unrecognized beachshirts service: %s\n", os.Args[1])
		os.Exit(1)
	}

	if serr := server.Start(); serr != nil {
		fmt.Println(serr.Error())
		os.Exit(1)
	}
}
