require "json"

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
          headers = {'host':'localhost'}
          client = Net::HTTP.new("localhost",3001)
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
          puts message
          status 503
          return message
        end
        style_name = params[:styleName]
        quantity = params[:quantity]

        if style_name && quantity
          headers = {'host':'localhost'}
          client = Net::HTTP.new("localhost",3001)
          path = "/style/" + style_name + "/make"
          req = Net::HTTP::Get.new(path, initheader=headers)
          req.set_form_data({'quantity':quantity})
          res = client.request(req)
          if res.kind_of? Net::HTTPSuccess
            status 200
            return  JSON.parse(res.body)
          else
            message = "Failed to order shirts!"
            puts "Failed to order shirts!"
            status res.code.to_i
            return message
          end
        else
          status 400
          "Missing field!"
        end
      end
    end
  end