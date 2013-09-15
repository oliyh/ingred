(ns ingred.store
  (:require [monger.core :as m]
            [monger.collection :as mc]
            [monger.joda-time]
            [cornerstone.config :refer [config]])
  (:import [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern]))

(def table "recipe")

(defn init
  "Connect to Mongo"
  []
  (m/connect-via-uri! (config :MONGOLAB_URI))
  (m/set-db! (m/get-db))) ;; db name is in the uri

(defn- replace-id [id m]
  (assoc (dissoc m :id) :_id id))

(defn save [m]
  (let [id (if-let [id (:id m)] (ObjectId. id) (ObjectId.))]
    (mc/update table {:_id id} (replace-id id m) :upsert true)
    {:id (str id)}))

(defn- replace-_id [m]
  (let [id (str (:_id m))]
    (assoc (dissoc m :_id) :id id)))

(defn load [id]
  (replace-_id (mc/find-map-by-id table (ObjectId. id))))

(defn list []
  (map replace-_id (mc/find-maps table)))
