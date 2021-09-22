(ns game.state
  (:require
   [clojure.string :as string]
   [reagent.core :as r]
   [game.config :as config]
   [game.builders :as builders]
   [game.util :refer [monster?
                      in?
                      adjacent-squares
                      coordinates->i
                      i->coordinates
                      clamp
                      max-stat-keyword]]))

;;;; STATE DATA
(def game-state (r/atom :PLAYING))

(def game-data (r/atom {:level 1}))

(def game-map (r/atom {}))

(def entities (r/atom  {}))

(def inventory (r/atom {:gold 0}))

(def events (r/atom '()))

(def menu (r/atom {:state :CLOSED}))

(def dropped-items (r/atom {}))

;;;; State Queries?
(defn all-items []
  (map last (-> @entities :player :items)))

(defn get-entity [id] (id @entities))

(defn get-player [] (get-entity :player))

(defn get-player-coordinates []
  (let [p (get-player)]
    [(:x p) (:y p)]))

(defn player-at? [x y]
  (let [p (get-player)]
    (and (= x (:x p)) (= y (:y p)))))

(defn player-adjacent? [x y]
  (let [squares (adjacent-squares x y)
        p (get-player)]
    (in? squares [(:x p) (:y p)])))

(defn get-player-tile-infront []
  (let [p (get-player)
        direction (:direction p)
        x (:x p)
        y (:y p)]
    (case direction
      :UP [x (dec y)]
      :DOWN [x (inc y)]
      :LEFT [(dec x) y]
      :RIGHT [(inc x) y]
      :default nil)))

(defn get-tile [x y]
  (get (:values @game-map) (+ x (* (:size @game-map) y))))

(defn tile-is? [type x y]
  (= (:type (get-tile x y)) type))

(defn all-entities []
  (map last @entities))

(defn all-monsters []
  (filter monster? (all-entities)))

(defn get-entity-at [x y]
  (some
   #(and (= (:x %) x) (= (:y %) y) %) (all-entities)))

(defn menu-open? []
  (= (:state @menu) :OPEN))

(defn get-item [entity-id item-id]
  (-> @entities entity-id :items item-id))

;;;; State Mutations ;;;;

;; Game Map ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn set-tile [x y type]
  (swap! game-map update-in [:values] (fn [tiles] (assoc tiles (coordinates->i (:size @game-map) x y) type))))

(defn open-door
  [x y]
  (set-tile x y config/blank))

(defn populate-map [m player-data]
  (let [size (count m)
        map-data (mapcat (fn [row] (map (fn [tile] (case tile
                                                     "-"  config/wall
                                                     "/"  config/door
                                                     "C"  config/closed-chest
                                                     "+"  config/stairs
                                                     config/blank)) row)) m)]
    (reset! game-map {:size size :values (vec map-data)})

    (doseq [[i tile] (map-indexed vector (string/join m))]
      (let [[x y] (i->coordinates size i)
            id (keyword (str (random-uuid)))]
        (case tile
          "@" (swap! entities assoc :player (builders/build-player (merge player-data {:x x
                                                                                       :y y})))
          "J" (swap! entities assoc id (builders/build-jelly {:id id
                                                              :x x
                                                              :y y}))
          "S" (swap! entities assoc id (builders/build-statue {:id id
                                                               :x x
                                                               :y y}))
          "X" (swap! entities assoc id (builders/build-soldier {:id id
                                                                :x x
                                                                :y y
                                                                :items {:2 {:id :2
                                                                            :variant :POTION
                                                                            :name "Soldier's Potion"
                                                                            :quantity 1
                                                                            :effects [{:effect :STAT-CHANGE :stat :health :amount 5}]}}}))
          nil)))))

;; Game State ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn end-game []
  (reset! game-state :GAME_OVER))

(defn increment-level []
  (swap! game-data update-in [:level] inc))

(defn next-level []
  (let [player (get-player)]
    (increment-level)
    (reset! entities {})
    (populate-map config/map1 player)))

;; UI ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn close-menu []
  (swap! menu assoc :state :CLOSED))

(defn open-menu []
  (swap! menu assoc :state :OPEN))

(defn add-event [text]
  (swap! events conj [(random-uuid) text]))

;; Player Inventory ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-gold [amount]
  (swap! inventory update-in [:gold] + amount))

;; items ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn place-items-on-tile [item-list [x y]]
  (swap! dropped-items update-in [[x y]] concat item-list))

;; Entities ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn remove-entity [id]
  (swap! entities dissoc id))

(defn set-entity-position [id x y]
  (swap! entities update-in [id] conj {:x x
                                       :y y}))

(defn set-entity-direction
  [entity-id direction]
  (swap! entities update-in [entity-id] conj {:direction direction}))

(defn update-entity-stat
  "Updates a numeric entity property by the provided amount. 
   Clamps the value to `max-{stat}`"
  [entity-id stat amount]
  (swap! entities update-in [entity-id stat]
         (fn [old-stat] (clamp (+ old-stat amount)
                               0
                               (-> @entities entity-id ((max-stat-keyword stat)))))))

(defn drop-items [entity-id]
  (let [{items :items
         x :x
         y :y} (get-entity entity-id)]
    (when (not-empty items)
      (place-items-on-tile (map last items) [x y]))))

(defn kill-entity
  [entity-id]
  (drop-items entity-id)
  (remove-entity entity-id))

(defn open-chest [x y]
  (let [amount (rand-int 100)]
    (add-event (str "You found " amount " gold in the chest!"))
    (add-gold amount)
    (set-tile x y config/opened-chest)))

;; Potion Effects ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti do-effect
  "Triggers a potions specific effect...
   
   :STAT-CHANGE - change a numeric entity stat by an amount"
  (fn [_ effect] (:effect effect)))

(defmethod do-effect :STAT-CHANGE [entity-id effect]
  (update-entity-stat entity-id (:stat effect) (:amount effect)))

(defn consume-item [entity-id item-id]
  (swap! entities update-in [entity-id :items item-id :quantity] dec)
  (when (<= (:quantity (get-item entity-id item-id)) 0)
    (swap! entities update-in [entity-id :items] dissoc item-id)))

(defn trigger-item-effects [entity-id item-id]
  (doseq [effect (:effects (get-item entity-id item-id))]
    (do-effect entity-id effect)))

(defn use-item [entity-id item-id]
  (trigger-item-effects entity-id item-id)
  (consume-item entity-id item-id))

;; Actions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn attack-action [src-id target-id]
  (let [{src-name :display-name
         damage :attack} (get-entity src-id)
        {target-name :display-name} (get-entity target-id)]
    (update-entity-stat target-id :health (- damage))
    (add-event (str src-name " hits " target-name " for " damage " damage!"))
    (when (<= (:health (get-entity target-id)) 0) (kill-entity target-id))))

(defn open-action []
  (let [[x y] (get-player-tile-infront)]
    (cond
      (tile-is? :DOOR x y) (open-door x y)
      (tile-is? :CLOSED-CHEST x y) (open-chest x y))))

(defn move-action [[x y] direction]
  (let [entity (get-entity-at x y)]
    (set-entity-direction :player direction)
    (cond
      (some? entity) (attack-action :player (:id entity))
      (tile-is? :STAIRS x y) (next-level)
      (tile-is? :BLANK x y) (set-entity-position :player x y)
      :else nil)))

(defn pickup-action [entity-id]
  (let [entity (get-entity entity-id)
        x (:x entity)
        y (:y entity)
        item-list (@dropped-items [x y])
        item-map (reduce (fn [acc x]
                           (assoc acc (:id x) x)) {} item-list)]
    (swap! entities update-in [entity-id :items] merge item-map)
    (swap! dropped-items dissoc [x y])
    (add-event (str "Picked up " (map :name item-list)))))

;; Other Effects ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn hunger-trigger [entity-id]
  (let [entity (get-entity entity-id)]
    (update-entity-stat entity-id :food -1)
    (when (<= (:food (get-entity entity-id)) 0)
      (kill-entity entity-id)
      (add-event (str (:display-name entity) " died from hunger.")))))
