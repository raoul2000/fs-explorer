(ns domain.say-hello)


(defn say-hello [name polite?]
  (format (if polite?
            "Good morning %s !"
            "Hi %s !") name))