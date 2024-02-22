(ns app-test
  (:require [cljs.test :refer (deftest is testing)]
            [utils :refer [href]]
            [routes :refer [init-routes!]]))


#_(init-routes!)

(deftest name-test
  (testing "dummy front test"
    (is (= true true))))

#_(deftest href-test
  (testing "href helper function"
    (is (= "assertion-values"
           (href :route/explore))))) 