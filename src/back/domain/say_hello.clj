(ns domain.say-hello)


(defn say-hello [name polite?]
  (if (= "bob" name)
    (throw (ex-info "user not allowed" {:name name}))
    (format (if polite?
              "Good morning %s !"
              "Hi %s !") (or name "stranger"))))