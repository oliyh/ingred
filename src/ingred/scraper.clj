(ns ingred.scraper
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as enlive]
            [clojure.string :as string]
            [ingred.processing :as processing]
            [ingred.store :as store]
            [cornerstone.config :as config])
  (:import [java.net URL]))

;; routes

(def letters "abcdefghijklmnopqrstuvwxyz")
(def bbc-root "http://www.bbc.co.uk")
(def cuisines-url (str bbc-root "/food/cuisines"))
(defn dishes-url [dish] (URL. (str bbc-root "/food/recipes/search?dishes[]=" (string/lower-case dish))))
(defn letter-url [letter] (URL. (str bbc-root "/food/dishes/by/letter/" letter)))

;; helpers

(def whitespace
  #(and (string? %) (string/blank? %)))

(defn tag [tag]
  #(and (map? %) (= tag (:tag %))))

(defn without-content [content & matchers]
  (remove #((apply some-fn matchers) %) content))

(defn the-first [content selector]
  (-> content enlive/html-resource (enlive/select [selector]) first))

;; alphabetical

(defn foods-for-letter [letter]
  (println "reading foods for" letter)
  (map (fn [%] {:id (-> % :attrs :id)
               :name (-> % enlive/text string/trim)
               :uri (-> % (the-first :a) :attrs :href)
               :type :food})
       (-> (letter-url letter) enlive/html-resource
           (enlive/select [:ol.foods :li.food]))))

;; for food ids

(defn recipes-for-food [food-id]
  (println "reading recipes for" food-id)
  (map (fn [%] {:name (-> % enlive/text)
               :uri (-> % :attrs :href)
               :type :recipe})
       (-> (dishes-url food-id) enlive/html-resource
           (enlive/select [:div#article-list :li.article :h3 :a]))))

;; the recipe

(defn read-recipe [uri]
  (println "reading recipe for" uri)
  (let [content (-> (str bbc-root uri) URL. enlive/html-resource)]
    {:name (-> content (enlive/select [:div.article-title :h1]) first enlive/text)
     :uri uri
     :preparation-time (-> content (enlive/select [:span.prepTime :span.value-title]) first :attrs :title)
     :cooking-time (-> content (enlive/select [:span.cookTime :span.value-title]) first :attrs :title)
     :yield (-> content (enlive/select [:h3.yield]) first enlive/text)
     :ingredients
     (map (fn [%] {:name (-> % (the-first :a) enlive/text)
                  :uri (-> % (the-first :a) :attrs :href)
                  :qty (-> % :content (without-content (tag :a)) first)
                  :preparation (-> % :content (without-content (tag :a)) second)
                  :type :ingredient})
          (-> content (enlive/select [:div#ingredients :p.ingredient])))
     :instructions
     (map (comp string/trim enlive/text)
          (-> content (enlive/select [:div#preparation :li.instruction])))}))


;; scraping

(defn init []
  (config/bootstrap {:name "dev"})
  (store/init))

(defn scrape-all []
  (init)
  (time
   (->> letters
        (take 1)
        (processing/pipe-seq (fn [x] (foods-for-letter x)) 4 1)
        processing/unfold
        (processing/pipe-seq (fn [x] (recipes-for-food (:id x))) 4 1)
        processing/unfold
        (processing/pipe-seq (fn [x] (read-recipe (:uri x))) 4 1)
        (processing/pipe-seq (fn [x] (store/save x)) 4 1))))


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
