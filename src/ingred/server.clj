(ns ingred.server
  (:require [ingred.ring :as handler]
            [ring.server.standalone :as ring-server]
            [cornerstone.config :as cfg]))

(defn start-server
  "used for starting the server in development mode from REPL, e.g. (def server (start-server))"
  ([] (start-server (cfg/bootstrap {:name "dev"})))
  ([config]
     (let [port (Integer. (or (get (System/getenv) "PORT" (config :ingred-port)) 8080))
           server (ring-server/serve (handler/get-handler #'handler/app)
                                     {:port port
                                      :init handler/init
                                      :auto-reload? true
                                      :destroy handler/destroy
                                      :join true
                                      :open-browser? false})]
       (println (str "You can view the site at http://localhost:" port "/"))
       server)))

(defn stop-server [server]
  (when server
    (.stop server)
    server))

(defn restart-server [server]
  (when server
    (doto server
      (.stop)
      (.start))))

(defn -main [& m]
  (let [mode (or (first m) "dev")]
    (let [server (start-server (cfg/bootstrap mode))]
      server)))
