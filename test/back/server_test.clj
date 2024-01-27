(ns server-test

  (:require [clojure.test :refer (deftest testing is)]
            [io.pedestal.http.route :as route]
            [server :as server]))

(route/try-routing-for server/routes :prefix-tree "/greet" :get)

(deftest greet-route-test
  (testing "route /greet"
    (let [{:keys [path method path-parts route-name]} 
          (route/try-routing-for server/routes :prefix-tree "/greet" :get)]
      (is (= "/greet" path))
      (is (= :get method))
      (is (= ["greet"] path-parts))
      (is (= :greet route-name))))) 