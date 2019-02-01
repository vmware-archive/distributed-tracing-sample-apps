require 'securerandom'
require 'net/http'

# Driver for Styling service which manages different styles of shirts and takes orders for a shirts
# of a given style.
#
# @author Yogesh Prasad Kurmi (ykurmi@vmware.com).
#
class Styling < Grape::API
    format :json
    resource :style do
        desc "List all styles"
        get do
          sleep(1)
          [
              {
                  "name": "Wavefront",
                  "url": "WavefrontURL"
              },
              {
                  "name": "BeachOps",
                  "url": "BeachOpsURL"
              }]
        end

        get '/:id/make' do
          if rand(1..5) == 5
            message = "Random Service Unavailable!"
            Rails.logger.warn(message)
            status 503
            return message
          end
          quantity = params[:quantity].to_i
          shirts = []
           0.upto(quantity) do
            shirts.push({"name": params[:id], "imageUrl":  params[:id] + "-image"})
          end
          order_num = SecureRandom.uuid
          headers = {'host':APP_CONFIG['styling']['host']}
          client = Net::HTTP.new(APP_CONFIG['delivery']['host'],APP_CONFIG['delivery']['port'])
          path = "/dispatch/" + order_num
          req = Net::HTTP::Post.new(path, initheader=headers)
          req.set_form_data({'shirts':shirts})
          res = client.request(req)
          if res.kind_of? Net::HTTPSuccess
            status 200
            return  JSON.parse(res.body)
          else
            message = "Failed to make shirts!"
            Rails.logger.warn(message)
            status res.code.to_i
            return message
          end
        end
    end
  end