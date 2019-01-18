# Wavefront Hackthon - Rails App

This is a sample Ruby application using Rails Framework called beachshirts (#beachops) which makes cool shirts for the beach.

### Running the Application Locally
1. git clone this repo and navigate to this dir:

    ```
     git clone https://github.com/wavefrontHQ/hackathon.git 
     cd hackathon/distributed-tracing/ruby-rails-app
    ```

2. Install the dependencies using below commands:
    ```
     ./shopping/bin/bundle install
     ./styling/bin/bundle install
     ./delivery/bin/bundle install
    ```

3. Start the services using below commands:
    ```
     ./shopping/bin/rails server
     ./styling/bin/rails server
     ./delivery/bin/rails server
    ```

4. Test the service by executing below API:
    ```
    http://localhost:3000/shop/menu
    ```
