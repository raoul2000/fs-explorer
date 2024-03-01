(ns server.response
  (:require [clojure.data.json :as json]
            [io.pedestal.http.content-negotiation :as conneg]))

(def default-content-type "application/json")
(defn- response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok                   (partial response 200))
(def error-SERVER_ERROR   (partial response 500))
(def error-NOT_FOUND      (partial response 404))
(def error-BAD_REQUEST    (partial response 400))

(defn error-body
  ([msg]
   {:error {:msg msg}})
  ([msg info]
   {:error {:msg msg :info info}}))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])
;; create a content negociator interceptor for the given list
;; of supported content types
(def content-neg-intc (conneg/negotiate-content supported-types))



(defn accepted-type
  "returns the accepted content type from the context map or `default-content-type` if not set"
  [context]
  (get-in context [:request :accept :field] default-content-type))

(defn transform-content
  "Converts and returns *body* into the given *content-type*"
  [body content-type]
  (case content-type
    "text/html"        body
    "text/plain"       body
    "application/edn"  (pr-str body)
    "application/json" (json/write-str body)))

(defn coerce-to
  "Updates and returns a response map given a *content-type*. The *response* body
   is coerced to the *content-type* and the Content-Type header is assigned the right value"
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(defn no-content-type?
  "Returns TRUE if the response header map doesn't contain any *Content-Type* key"
  [context]
  (nil? (get-in context [:response :headers "Content-Type"])))

;; interceptor with only a 'leave' handler dedicated to be executed last to
;; convert the response body into the best match for accepted content type
;; as it was computed by content negociation interceptor (content-neg-intc)
(def coerce-body
  {:name ::coerce-body
   :leave (fn [context]
            (cond-> context
              (no-content-type? context) (update-in [:response] coerce-to (accepted-type context))))})


