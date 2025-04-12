(ns elin-mcp.core
  (:require
   [clj-mcp.core :as mcp]
   [clojure.data.json :as json])
  (:gen-class))

(defn- api-request
  [{:keys [port method params]}]
  (let [uri (java.net.URI/create
              (str  "http://localhost:" port "/api/v1"))
        body (json/write-str {:method (str method)
                              :params (map str params)})
        client (java.net.http.HttpClient/newHttpClient)
        req (-> (java.net.http.HttpRequest/newBuilder)
                (.uri uri)
                (.header "Content-Type" "application/json")
                (.POST (java.net.http.HttpRequest$BodyPublishers/ofString body))
                (.build))
        res (.send client req (java.net.http.HttpResponse$BodyHandlers/ofString))]
    (when (= 200 (.statusCode res))
      (json/read-str
        (.body res)))))

(defn- evaluate-clojure-code
  "Evaluate specified clojure code with elin.
   Port is defined in g:elin_http_server_port on vim/neovim."
  [port code]
  (try
    (api-request {:port port
                  :method "elin.handler.evaluate/evaluate"
                  :params [code]})
    (catch Exception ex
      (format "Failed to evaluate code: %s" (ex-message ex)))))

(defn- lookup-clojure-symbol
  "Lookup specified clojure symbol with elin.
   Port is defined in g:elin_http_server_port on vim/neovim."
  [port symbol]
  (try
    (api-request {:port port
                  :method "elin.handler.lookup/lookup"
                  :params [symbol]})
    (catch Exception ex
      (format "Failed to lookup symbol: %s" (ex-message ex)))))

(defn- show-clojure-source
  "Show clojure source for specified clojure symbol with elin.
   Port is defined in g:elin_http_server_port on vim/neovim."
  [port symbol]
  (try
    (api-request {:port port
                  :method "elin.handler.lookup/show-source"
                  :params [symbol]})
    (catch Exception ex
      (format "Failed to show source: %s" (ex-message ex)))))

(defn -main
  [& _]
  (mcp/start {:name "elin-mcp"
              :version "0.0.1"
              :tool-vars [#'evaluate-clojure-code
                          #'lookup-clojure-symbol
                          #'show-clojure-source]}))
