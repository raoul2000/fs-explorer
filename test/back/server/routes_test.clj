(ns server.routes-test
  (:require [clojure.test :refer (deftest testing is)]
            [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :refer [response-for]]
            [server.routes :as server-routes]
            [server.response :refer (default-content-type)]
            [system :as sys]
            [clojure.data.json :as json]))

;; helper functions -------------------------------------------------------------

;; Use this function is test so to refer to a route by its name better than
;; by its path
(def test-routes (sys/init-server-routes {}))
(def url-for (route/url-for-routes test-routes))
(def service-map {::http/routes            test-routes
                  ::http/resource-path     "/public"
                  ::http/port              8890
                  ::http/type              :jetty
                  ::http/join?             true})
(def service (:io.pedestal.http/service-fn (http/create-servlet service-map)))


(comment

  (type service)
  (url-for :home)
  (dissoc (response-for service :get (url-for :home)) :body)
  (response-for service :get (url-for :greet) :headers {"Accept" "application/json"})
  (response-for service :get (url-for :greet) :headers {"Accept" "application/edn"})
  (response-for service :get (url-for :greet) :headers {"Accept" "text/text"})

  (url-for :greet :query-params {:name  "bob"})
  (:body (response-for service :get (url-for :greet :query-params {:name  "bob"})))


  (url-for :greet :request {:headers {:content-type "application/json"}})
  (url-for :explorer :path-params {:path "folder"})

  ;;
  )

(defn content-type [response]
  (get-in response [:headers "Content-Type"]))

;; test ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest greet-route-test

  (testing "routing for /greet"
    (let [{:keys [path method path-parts route-name]}
          (route/try-routing-for (server-routes/create {:polite? true})  :prefix-tree "/greet" :get)]
      (is (= "/greet" path))
      (is (= :get method))
      (is (= ["greet"] path-parts))
      (is (= :greet route-name))))

  (testing "response content-type for /greet accept (content neg)"

    (let [response (response-for service :get (url-for :greet))]
      (is (= 200 (:status response))
          "success response")
      (is (=  default-content-type (content-type response))
          (str "default content-type is " default-content-type)))

    (let [response (response-for service :get (url-for :greet) :headers {"Accept" "application/json"})]
      (is (= 200 (:status response))
          "success response")
      (is (= "application/json" (content-type response))
          "content-type JSON is supported"))

    (let [response (response-for service :get (url-for :greet) :headers {"Accept" "application/edn"})]
      (is (= 200 (:status response))
          "success response")
      (is (= "application/edn" (content-type response))
          "content-type EDN is supported"))

    (let [response (response-for service :get (url-for :greet) :headers {"Accept" "text/plain"})]
      (is (= 200 (:status response))
          "success response")
      (is (= "text/plain" (content-type response))
          "content-type Text/Plain is supported")))

  (testing "error response for bob"
    (let [response (response-for service :get (url-for :greet :query-params {:name "bob"}))]
      (is (= 400 (:status response))
          "logic error returns code 400")
      (is (= {"error" {"msg" "user not allowed",
                       "info"    {"name" "bob"}}} (json/read-str (:body response)))
          "logic error returns json body with logic message and info")))) 


