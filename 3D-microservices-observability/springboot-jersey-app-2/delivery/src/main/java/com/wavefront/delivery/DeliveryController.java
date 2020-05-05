package com.wavefront.delivery;

import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ResponseDTO;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeliveryController {

  /*
   * TODO: Add a gauge to monitor the size of dispatch queue.
   * Also, consider adding relevant ApplicationTags for this metric.
   */
  private static Queue<PackedShirtsDTO> dispatchQueue ;
  protected Logger logger = Logger.getLogger(DeliveryController.class.getName());
  static {
    dispatchQueue = new ConcurrentLinkedDeque<>();
  }

  @GetMapping(path="/hello")
  public ResponseDTO hello() {
    return ResponseDTO.ok().msg("hello from Delivery Service").build();
  }

  @PostMapping(path="/dispatch/{orderNum}")
  public ResponseDTO dispatch(@RequestBody PackedShirtsDTO packedShirts, @PathVariable("orderNum") String orderNum) {
    ResponseDTO response = new ResponseDTO();
    if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
      String msg = "Failed to dispatch shirts!";
      logger.warning(msg);
      return ResponseDTO.status("SERVICE UNAVAILABLE").msg(msg).build();
    }
    if (orderNum.isEmpty()) {
      /*
       * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
       */
      String msg = "Invalid Order Num";
      logger.warning(msg);
      return ResponseDTO.status("BAD REQUEST").msg(msg).build();
    }
    if (packedShirts == null || packedShirts.getShirts() == null ||
        packedShirts.getShirts().size() == 0) {
      /*
       * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
       */
      String msg = "No shirts to deliver";
      logger.warning(msg);
      return ResponseDTO.status("BAD REQUEST").msg(msg).build();
    }
    dispatchQueue.add(packedShirts);
    String trackingNum = UUID.randomUUID().toString();
    return ResponseDTO.ok().entity(new DeliveryStatusDTO(orderNum, trackingNum,
        "shirts delivery dispatched"));
  }

  @GetMapping(path="/retrieve/{orderNum}")
  public ResponseDTO retrieve(@PathVariable("orderNum") String orderNum) {
    if (orderNum.isEmpty()) {
      /*
       * TODO: Try to emitting an error metrics with relevant ApplicationTags to Wavefront.
       */
      String msg = "Invalid Order Num";
      logger.warning(msg);
      return ResponseDTO.status("BAD REQUEST").msg(msg).build();
    }
    return ResponseDTO.ok().msg("Order: " + orderNum + " returned").build();
  }

  @Scheduled(fixedRate = 30000)
  private void processQueue() {
    System.out.println("Processing " + dispatchQueue.size() + " in the Dispatch Queue!");
    while (!dispatchQueue.isEmpty()) {
      deliverPackedShirts(dispatchQueue.poll());
    }
  }

  private void deliverPackedShirts(PackedShirtsDTO packedShirtsDTO) {
    for (int i = 0; i < packedShirtsDTO.getShirts().size(); i++) {
      /*
       * TODO: Try to Increment a delta counter when shirts are delivered.
       * Also, consider adding relevant ApplicationTags for this metric.
       */
    }
    System.out.println(packedShirtsDTO.getShirts().size() + " shirts delivered!");
  }
}
