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
  ;;(println "reading foods for" letter)
  (map (fn [%] {:id (-> % :attrs :id)
               :name (-> % enlive/text string/trim)
               :uri (-> % (the-first :a) :attrs :href)
               :type :food
               :letter letter})
       (-> (letter-url letter) enlive/html-resource
           (enlive/select [:ol.foods :li.food]))))

;; for food ids

(defn recipes-for-food [{:keys [id] :as food}]
  ;;(println "reading recipes for" id)
  (map (fn [%] {:name (-> % enlive/text)
               :uri (-> % :attrs :href)
               :food food
               :type :recipe})
       (-> (dishes-url id) enlive/html-resource
           (enlive/select [:div#article-list :li.article :h3 :a]))))

;; the recipe

(defn read-recipe [{:keys [uri food] :as recipe}]
  (println "reading recipe for" uri)
  (let [content (-> (str bbc-root uri) URL. enlive/html-resource)]
    {:name (-> content (enlive/select [:div.article-title :h1]) first enlive/text)
     :id (last (clojure.string/split uri #"/"))
     :food food
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

(defn scrape-all
  ([] (scrape-all (take 1 letters)))
  ([letters]
     (let [total (atom 0)
           complete (atom 0)]
       (future
         (time
          (doall
           (->> letters
                (processing/pipe-seq (fn [x] (foods-for-letter x)) 4 1)
                processing/unfold
                (processing/pipe-seq
                 (fn [x]
                   (let [recipes (recipes-for-food x)]
                     (swap! total + (count recipes))
                     recipes))
                 4 1)
                processing/unfold
                (processing/pipe-seq (fn [x] (read-recipe x)) 4 1)
                (processing/pipe-seq
                 (fn [x]
                   (swap! complete inc)
                   (println "saved" (:uri x))
                   (:id (store/save x)))
                 4 1)))))
       {:total total :complete complete})))

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
