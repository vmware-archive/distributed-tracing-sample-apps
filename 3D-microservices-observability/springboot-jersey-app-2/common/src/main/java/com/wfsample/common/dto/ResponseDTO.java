package com.wfsample.common.dto;

public class ResponseDTO {
  private String status;
  private String message;
  private DeliveryStatusDTO deliveryStatusDTO;
  private OrderDTO orderDTO;
  private OrderStatusDTO orderStatusDTO;
  private PackedShirtsDTO packedShirtsDTO;
  private ShirtDTO shirtDTO;
  private ShirtStyleDTO shirtStyleDTO;


  public String getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public DeliveryStatusDTO getDeliveryStatusDTO() {
    return deliveryStatusDTO;
  }

  public void setDeliveryStatusDTO(DeliveryStatusDTO deliveryStatusDTO) {
    this.deliveryStatusDTO = deliveryStatusDTO;
  }

  public OrderDTO getOrderDTO() {
    return orderDTO;
  }

  public void setOrderDTO(OrderDTO orderDTO) {
    this.orderDTO = orderDTO;
  }

  public OrderStatusDTO getOrderStatudDTO() {
    return orderStatusDTO;
  }

  public void setOrderStatudDTO(OrderStatusDTO orderStatudDTO) {
    this.orderStatusDTO = orderStatudDTO;
  }

  public PackedShirtsDTO getPackedShirtsDTO() {
    return packedShirtsDTO;
  }

  public void setPackedShirtsDTO(PackedShirtsDTO packedShirtsDTO) {
    this.packedShirtsDTO = packedShirtsDTO;
  }

  public ShirtDTO getShirtDTO() {
    return shirtDTO;
  }

  public void setShirtDTO(ShirtDTO shirtDTO) {
    this.shirtDTO = shirtDTO;
  }

  public ShirtStyleDTO getShirtStyleDTO() {
    return shirtStyleDTO;
  }

  public void setShirtStyleDTO(ShirtStyleDTO shirtStyleDTO) {
    this.shirtStyleDTO = shirtStyleDTO;
  }

  public static ResponseDTO ok() {
    ResponseDTO response = new ResponseDTO();
    response.setStatus("OK");
    return response;
  }

  public ResponseDTO entity(OrderStatusDTO statusDTO) {
    this.orderStatusDTO = statusDTO;
    return this;
  }

  public ResponseDTO entity(DeliveryStatusDTO statusDTO) {
    this.deliveryStatusDTO = statusDTO;
    return this;
  }

  public ResponseDTO entity(OrderDTO orderDTO) {
    this.orderDTO = orderDTO;
    return this;
  }

  public ResponseDTO entity(PackedShirtsDTO packedDTO) {
    this.packedShirtsDTO = packedDTO;
    return this;
  }

  public ResponseDTO msg(String msg) {
    this.message = msg;
    return this;
  }

  public static ResponseDTO status(String status) {
    ResponseDTO response = new ResponseDTO();
    response.setStatus(status);
    return response;
  }

  public ResponseDTO build() {
    return this;
  }
}
