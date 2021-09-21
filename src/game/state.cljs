(ns game.state
  (:require
   [reagent.core :as r]
   [game.canvas :as canvas]
   [game.config :as config]
   [game.util :refer [monster?
                      in?
                      adjacent-squares
                      coordinates->i
                      i->coordinates
                      add-coordinates
                      clamp
                      max-stat-keyword]]))

;;;; STATE DATA
(def game-state (atom :PLAYING))

(def game-map (atom {}))

(def entities (r/atom  {}))

(def inventory (r/atom {:gold 0}))

(def events (r/atom '()))

(def menu (r/atom {:state :CLOSED}))

(def items (r/atom {[1 4] [{:id :3
                            :variant :POTION
                            :name "Random Potion"
                            :quantity 1
                            :effects [{:effect :STAT-CHANGE :stat :health :amount 5}]}]}))

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

;;;; State Mutations
(defn update-entity-stat [entity-id stat amount]
  (swap! entities update-in [entity-id stat]
         (fn [old-stat] (clamp (+ old-stat amount)
                               0
                               (-> @entities entity-id ((max-stat-keyword stat)))))))


(defmulti do-effect (fn [_ effect] (:effect effect)))
(defmethod do-effect :STAT-CHANGE [entity-id effect]
  (update-entity-stat entity-id (:stat effect) (:amount effect)))

(defn use-item [entity-id item-id]
  (let [item (get-item entity-id item-id)
        effects (:effects item)]
    (swap! entities update-in [entity-id :items item-id :quantity] dec)
    (when (<= (:quantity (get-item entity-id item-id)) 0)
      (swap! entities update-in [entity-id :items] dissoc item-id))
    (doseq [effect effects]
      (do-effect entity-id effect))))

(defn close-menu []
  (swap! menu assoc :state :CLOSED))

(defn open-menu []
  (swap! menu assoc :state :OPEN))

(defn add-event [text]
  (swap! events conj [(random-uuid) text]))

(defn add-gold [amount]
  (swap! inventory update-in [:gold] + amount))

(defn move-entity [id x y]
  (swap! entities update-in [id] conj {:x x
                                       :y y}))

(defn set-direction [direction]
  (swap! entities update-in [:player] conj {:direction direction}))

(defn end-game []
  (reset! game-state :GAME_OVER))

(defn place-items-on-tile [item-list [x y]]
  (swap! items update-in [[x y]] concat item-list))

(defn kill-entity [id]
  (let [entity (-> @entities id)]
    (when (not-empty (:items entity))
      (place-items-on-tile (vec (map last (:items entity))) [(:x entity) (:y entity)]))
    (swap! entities dissoc id)))

(defn open-door [x y]
  (swap! game-map update-in [:values] (fn [tiles] (assoc tiles (coordinates->i (:size @game-map) x y) config/blank))))

(defn open-chest [x y]
  (let [amount (rand-int 100)]
    (add-event (str "You found " amount " gold in the chest!"))
    (add-gold amount))
  (swap! game-map update-in [:values] (fn [tiles] (assoc tiles (coordinates->i (:size @game-map) x y) config/opened-chest))))

(defn perform-attack [src-id target-id]
  (let [src (get-entity src-id)
        target (get-entity target-id)
        src-name (:display-name src)
        target-name (:display-name target)
        damage (:attack src)]
    (update-entity-stat target-id :health (- damage))
    (add-event (str src-name " hits " target-name " for " damage " damage!"))
    (when (<= (:health (get-entity target-id)) 0) (kill-entity target-id))))

(defn try-open []
  (let [[x y] (get-player-tile-infront)]
    (cond
      (tile-is? :DOOR x y) (open-door x y)
      (tile-is? :CLOSED-CHEST x y) (open-chest x y))))

(defn try-move [[x y] direction]
  (let [entity (get-entity-at x y)]
    (set-direction direction)
    (cond
      (some? entity) (perform-attack :player (:id entity))
      (tile-is? :BLANK x y) (move-entity :player x y)
      :else nil)))

(defn pick-up-item [entity-id]
  (let [entity (get-entity entity-id)
        x (:x entity)
        y (:y entity)
        item-list (@items [x y])
        item-map (reduce (fn [acc x]
                           (assoc acc (:id x) x)) {} item-list)]
    (swap! entities update-in [entity-id :items] merge item-map)
    (swap! items dissoc [x y])
    (add-event (str "Picked up " (map :name item-list)))))

(defn try-pickup []
  (pick-up-item :player))

;;;; RENDERING
(defn coordinates->pixels [[x y]]
  [(* x config/TILE-SIZE) (* y config/TILE-SIZE)])

(defmulti render-tile (fn [tile _] (:type tile)))

(defmethod render-tile :default [tile coordinates]
  (let [[x y] (coordinates->pixels coordinates)]
    (canvas/draw-rect x y config/TILE-SIZE config/TILE-SIZE (:color tile))))



(defn calculate-scroll-offset-from-player [x y]
  [(clamp (Math/floor (- x (/ config/tiles-per-screen-x 2)))
          0
          (Math/max 0 (- (:size @game-map) config/tiles-per-screen-x)))
   (clamp (Math/floor (- y (/ config/tiles-per-screen-y 2)))
          0
          (Math/max 0 (- (:size @game-map) config/tiles-per-screen-y)))])

(defn render-map []
  (doseq [[i tile] (map-indexed vector (:values @game-map))]
    (let [coordinates (i->coordinates (:size @game-map) i)
          [player-x player-y] (get-player-coordinates)
          [x-offset y-offset] (calculate-scroll-offset-from-player player-x player-y)]
      (render-tile
       tile
       (add-coordinates coordinates [(- x-offset) (- y-offset)])))))

(defn render-entity [entity]
  (let [[player-x player-y] (get-player-coordinates)
        [x-offset y-offset] (calculate-scroll-offset-from-player player-x player-y)]
    (canvas/draw-rect
     (* config/TILE-SIZE (- (:x entity) x-offset))
     (* config/TILE-SIZE (- (:y entity) y-offset))
     config/TILE-SIZE config/TILE-SIZE
     (:color entity))))

(defn render-entities []
  (doseq [entity (all-entities)]
    (render-entity entity)))

(defn render-items []
  (let [[player-x player-y] (get-player-coordinates)
        [x-offset y-offset] (calculate-scroll-offset-from-player player-x player-y)]
    (doseq [[[x y] item] @items]
      (when (not-empty item) (canvas/draw-rect
                              (* config/TILE-SIZE (- x x-offset))
                              (* config/TILE-SIZE (- y y-offset))
                              config/TILE-SIZE config/TILE-SIZE
                              "cornflowerblue")))))
