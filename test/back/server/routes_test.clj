(ns server.routes-test
  (:require [clojure.test :refer (deftest testing is)]
            [io.pedestal.http.route :as route]
            [server.routes :as server-routes]))

(deftest greet-route-test
  (testing "route /greet"
    (let [{:keys [path method path-parts route-name]}
          (route/try-routing-for (server-routes/create {:polite? true})  :prefix-tree "/greet" :get)]
      (is (= "/greet" path))
      (is (= :get method))
      (is (= ["greet"] path-parts))
      (is (= :greet route-name))))) 
