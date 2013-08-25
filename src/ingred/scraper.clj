(ns ingred.scraper
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as enlive]
            [clojure.string :as string])
  (:import [java.net URL]))

(def letters "abcdefghijklmnopqrstuvwxyz")
(def bbc-root "http://www.bbc.co.uk")
(def cuisines-url (str bbc-root "/food/cuisines"))
(defn dishes-url [dish] (URL. (str bbc-root "/food/recipes/search?dishes[]=" (string/lower-case dish))))
(defn letter-url [letter] (URL. (str bbc-root "/food/dishes/by/letter/" letter)))

(def whitespace
  #(and (string? %) (string/blank? %)))

(defn tag [tag]
  #(and (map? %) (= tag (:tag %))))

(defn without-content [content & matchers]
  (remove #((apply some-fn matchers) %) content))

(defn the-first [content selector]
  (-> content enlive/html-resource (enlive/select [selector]) first))

;; alphabetical

(defn for-letter [letter]
  (map (fn [%] {:id (-> % :attrs :id)
               :name (-> % enlive/text string/trim)
               :uri (-> % (the-first :a) :attrs :href)
               :type :food})
       (-> (letter-url letter) enlive/html-resource (enlive/select [:ol.foods :li.food]))))

;; for food ids

(defn for-food [food-id]
  (map (fn [%] {:name (-> % enlive/text)
               :uri (-> % :attrs :href)})
       (-> (dishes-url food-id) enlive/html-resource
           (enlive/select [:div#article-list :li.article :h3 :a]))))



;; by cuisine

(defn- index-for [cuisine]
  (str bbc-root "/food/recipes/search?keywords=&cuisines[]=" cuisine "&occasions[]=&chefs[]=&programmes[]="))

(defn- cuisine-name [a]
  (-> a enlive/html-resource (enlive/select [:span]) first :content first))

(defn cuisines []
  (map #(vector (cuisine-name %) (->  % :attrs :href))
       (-> cuisines-url URL. enlive/html-resource
           (enlive/select [:ol#cuisines :li :h3 :a]))))

(defn recipes-for [cuisine]
  (map #(vector (:content %) (-> % :attrs :href))
       (-> (str bbc-root (second (first (cuisines)))) URL. enlive/html-resource
           (enlive/select [:ul.resources :li :h4 :a]))))
