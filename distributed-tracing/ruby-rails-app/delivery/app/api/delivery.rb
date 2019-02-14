require 'securerandom'

# Driver for Delivery service which manages the delivery of shirts.
#
# @author Yogesh Prasad Kurmi (ykurmi@vmware.com).
#
class Delivery < Grape::API
    format :json
    resource :dispatch do
        desc "Dispatch the order"
        use Rack::Tracer
        post '/:order_num' do
          sleep(1)
          order_num = params[:order_num]
          if rand(1..5) == 5
            message = "Random Service Unavailable!"
            Rails.logger.warn(message)
            env['rack.span'].set_tag('error', true)
            status 503
            return message
          end
          if rand(1..10) == 10
            order_num = nil
          end

          if !order_num
            message = "Invalid Order Num!"
            Rails.logger.warn(message)
            env['rack.span'].set_tag('error', true)
            status 400
            return message
          end
          packed_shirts = nil
          if rand(1..10) !=  10
            packed_shirts = params[:shirts]
          end

          if !packed_shirts
            message = "No shirts to deliver!"
            Rails.logger.warn(message)
            env['rack.span'].set_tag('error', true)
            status 400
            return message
          end

          tracking_num = SecureRandom.uuid
          status 200
          {"orderNum": order_num,
           "trackingNum": tracking_num,
           "status": "shirts delivery dispatched"}
        end
  
    end
    resource :retrieve do
      desc "Retrieve Order"
      use Rack::Tracer
      post '/:order_num' do
        sleep(1)
        order_num = params[:order_num]
        if !order_num
          message = "Invalid Order Num!"
          Rails.logger.warn(message)
          env['rack.span'].set_tag('error', true)
          status 400
          return message
        end
        status 200
        {"status": "Order:" + order_num + " returned"}
      end
    end
  end