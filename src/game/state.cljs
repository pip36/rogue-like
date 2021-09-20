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
                      clamp]]))

;;;; STATE DATA
(def game-state (atom :PLAYING))

(def game-map (atom {}))

(def entities (r/atom  {}))

(def inventory (r/atom {:gold 0}))

(def events (r/atom '()))

(def menu (r/atom {:state :CLOSED}))

;;;; State Queries?
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

;;;; State Mutations
(defn close-menu []
  (swap! menu assoc :state :CLOSED))

(defn open-menu []
  (swap! menu assoc :state :OPEN))

(defn add-event [text]
  (swap! events conj text))

(defn add-gold [amount]
  (swap! inventory update-in [:gold] + amount))

(defn move-entity [id x y]
  (swap! entities update-in [id] conj {:x x
                                       :y y}))

(defn set-direction [direction]
  (swap! entities update-in [:player] conj {:direction direction}))

(defn end-game []
  (reset! game-state :GAME_OVER))

(defn hurt-entity [id amount]
  (swap! entities update-in [id :health] - amount))

(defn kill-entity [id]
  (swap! entities dissoc id))

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
    (hurt-entity target-id damage)
    (add-event (str src-name " hits " target-name " for " damage " damage!"))
    (when (<= (:health target) 0) (kill-entity target-id))))

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
