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
        use Rack::Tracer
        get do
          headers = {'host':APP_CONFIG['shopping']['host']}
          client = Net::HTTP.new(APP_CONFIG['styling']['host'],APP_CONFIG['styling']['port'])
          req = Net::HTTP::Get.new("/style", initheader=headers)
          OpenTracing.inject(env['rack.span'].context, OpenTracing::FORMAT_RACK, req)
          res = client.request(req)
          if res.kind_of? Net::HTTPSuccess
            status 200
            return JSON.parse(res.body)
          else
            env['rack.span'].set_tag('error', true)
            return status res.code.to_i
          end
        end
    end
    resource :order do
      desc "Accept order from user"
      use Rack::Tracer
      post do
        if rand(1..5) == 5
          message = "Random Service Unavailable!"
          Rails.logger.warn(message)
          env['rack.span'].set_tag('error', true)
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
          OpenTracing.inject(env['rack.span'].context, OpenTracing::FORMAT_RACK, req)
          res = client.request(req)
          if res.kind_of? Net::HTTPSuccess
            status 200
            return  JSON.parse(res.body)
          else
            message = "Failed to order shirts!"
            Rails.logger.warn(message)
            env['rack.span'].set_tag('error', true)
            status res.code.to_i
            return message
          end
        else
          message = "Missing field!"
          Rails.logger.warn(message)
          env['rack.span'].set_tag('error', true)
          status 400
          return message
        end
      end
    end
  end