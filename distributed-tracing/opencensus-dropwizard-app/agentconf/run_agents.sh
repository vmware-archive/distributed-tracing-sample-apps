#!/bin/sh

# ./run_agents <opencensus_agent_binary>

echo "Starting agents..."

$1 -c ShoppingService.yml & waits+=($!)
$1 -c StylingService.yml & waits+=($!)
$1 -c DeliveryService.yml & waits+=($!)

# trap "echo Press Enter to quit..." SIGINT

read -p $'Press Enter to quit...\n'

echo "Quitting..."
kill "${waits[@]}" 

