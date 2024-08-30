(ns config-test
  (:require [clojure.test :refer (deftest testing is are)]
            [clojure.spec.alpha :as spec]
            [config :as conf]))

((deftest spec-test
   (testing "server-port validation"
     (are [result arg-map] (= result (spec/valid? :config/server-port (:port arg-map)))
       true      {:port 3000}
       false     {:port -1}
       false     {:port 1.25}
       false     {:port "8888"}))

   (testing "selectors"
     (is (not (spec/valid? :config.type/selectors {:starts-with "str"
                                                   :ends-with  "txt"}))
         "selectors is not a map anymore")

     (is (spec/valid? :config.type/selectors [{:starts-with "str"}
                                              {:ends-with  "txt"}])
         "selectors is a collection of maps")

     (is (not (spec/valid? :config.type/selectors []))
         "selectors can't empty"))

   (testing "type definition"
     (is (not (spec/valid? :config.type/definition #:config.type{}))
         "empty type definition is not allowed")

     (is (spec/valid? :config.type/definition #:config.type{:name "type name"})
         "type name is required")

     (is (spec/valid? :config.type/definition #:config.type{:name "type name"
                                                            :selector [{:starts-with "str"}
                                                                       {:ends-with  "txt"}]})
         "selectors when configured must not be empty"))

   (testing "types config"
     (is (not (spec/valid? :config/types []))
         "empty type config is not allowed")

     (is (spec/valid? :config/types [#:config.type{:name "type name"}
                                     #:config.type{:name "other type name"}])))))
