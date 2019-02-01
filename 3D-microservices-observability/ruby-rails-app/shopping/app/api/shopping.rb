require "json"
require 'net/http'

 # Driver for Shopping service provides consumer facing APIs supporting activities like browsing
 # different styles of beachshirts, and ordering beachshirts.
 #
 # @author Yogesh Prasad Kurmi (ykurmi@vmware.com).
 #
class Shopping < Grape::API
    prefix 'shop'
    format :json
    resource :menu do
        desc "List shopping menu"
        get do
          headers = {'host':APP_CONFIG['shopping']['host']}
          client = Net::HTTP.new(APP_CONFIG['styling']['host'],APP_CONFIG['styling']['port'])
          req = Net::HTTP::Get.new("/style", initheader=headers)
          res = client.request(req)
          if res.kind_of? Net::HTTPSuccess
            status 200
            return JSON.parse(res.body)
          else
            return status res.code.to_i
          end
        end
    end
    resource :order do
      desc "Accept order from user"
      post do
        if rand(1..5) == 5
          message = "Random Service Unavailable!"
          Rails.logger.warn(message)
          status 503
          return message
        end
        style_name = params[:styleName]
        quantity = params[:quantity]

        if style_name && quantity
          headers = {'host':APP_CONFIG['shopping']['host']}
          client = Net::HTTP.new(APP_CONFIG['styling']['host'],APP_CONFIG['styling']['port'])
          path = "/style/" + style_name + "/make"
          req = Net::HTTP::Get.new(path, initheader=headers)
          req.set_form_data({'quantity':quantity})
          res = client.request(req)
          if res.kind_of? Net::HTTPSuccess
            status 200
            return  JSON.parse(res.body)
          else
            message = "Failed to order shirts!"
            Rails.logger.warn(message)
            status res.code.to_i
            return message
          end
        else
          message = "Missing field!"
          Rails.logger.warn(message)
          status 400
          return message
        end
      end
    end
  end