(ns game.state
  (:require
   [reagent.core :as r]
   [game.canvas :as canvas]
   [game.config :as config]
   [game.util :refer [monster? in? adjacent-squares coordinates->i add-coordinates]]))

;;;; STATE DATA
(def game-state (atom :PLAYING))

(defn wall []
  {:type :WALL
   :color "black"})

(defn blank []
  {:type :BLANK
   :color "white"})

(defn door []
  {:type :DOOR
   :color "orange"})

(def game-map (atom
               {:size 15
                :values [(wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (wall) (wall) (wall) (door) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (door) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (door) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
                         (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall)]}))

(def entities (r/atom  {}))

(def events (r/atom '()))

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

;;;; Monster Movement
(defmulti movement :movement)

(defmethod movement :STATIC
  [monster]
  [(:x monster) (:y monster)])

(defmethod movement :RANDOM
  [monster]
  (let [x (:x monster)
        y (:y monster)
        choices (adjacent-squares x y)
        [x2 y2] (get choices (rand-int 4))]
    (cond
      (player-at? x2 y2) [x y]
      (tile-is? :BLANK x2 y2) [x2 y2]
      :else [x y])))


;;;; State Mutations
(defn add-event [text]
  (swap! events conj text))

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
  (swap! entities dissoc id)
  (add-event (str id "died")))

(defn open-door [x y]
  (add-event (str "Opened door"))
  (swap! game-map update-in [:values] (fn [tiles] (assoc tiles (coordinates->i (:size @game-map) x y) (blank)))))

(defn perform-attack [src target]
  (add-event (str src "attacked" target))
  (hurt-entity target (:attack (get-entity src)))
  (when (<= (:health (get-entity target)) 0) (kill-entity target)))

;;;; Logic
(defn update-monsters
  "Loop through all monsters and trigger their movement function."
  []
  (doseq [monster (all-monsters)]
    (cond
      (player-adjacent? (:x monster) (:y monster)) (perform-attack (:id monster) :player)
      :else (let [[x y] (movement monster)]
              (move-entity (:id monster) x y)))))

(defn try-move [[x y] direction]
  (let [entity (get-entity-at x y)]
    (set-direction direction)
    (cond
      (some? entity) (perform-attack :player (:id entity))
      (tile-is? :BLANK x y) (move-entity :player x y)
      :else nil)))

(defn try-open []
  (let [[x y] (get-player-tile-infront)]
    (when (tile-is? :DOOR x y)
      (open-door x y))))

;;; INPUT
(defn handle-user-update [key]
  (case key
    :UP (try-move (add-coordinates (get-player-coordinates) [0 -1]) :UP)
    :DOWN (try-move (add-coordinates (get-player-coordinates) [0 1]) :DOWN)
    :LEFT (try-move (add-coordinates (get-player-coordinates) [-1 0]) :LEFT)
    :RIGHT (try-move (add-coordinates (get-player-coordinates) [1 0]) :RIGHT)
    :O (try-open)
    :default nil))

;;;; RENDERING
(defn i->coordinates [i]
  [(mod i (:size @game-map)) (Math/floor (/ i (:size @game-map)))])

(defn coordinates->pixels [[x y]]
  [(* x config/TILE-SIZE) (* y config/TILE-SIZE)])

(defmulti render-tile (fn [tile _] (:type tile)))

(defmethod render-tile :default [tile coordinates]
  (let [[x y] (coordinates->pixels coordinates)]
    (canvas/draw-rect x y config/TILE-SIZE config/TILE-SIZE (:color tile))))

(defn render-map []
  (doseq [[i tile] (map-indexed vector (:values @game-map))]
    (let [coordinates (i->coordinates i)]
      (render-tile
       tile
       coordinates))))

(defn render-entity [entity]
  (canvas/draw-rect
   (* config/TILE-SIZE (:x entity))
   (* config/TILE-SIZE (:y entity))
   config/TILE-SIZE config/TILE-SIZE
   (:color entity)))


(defn render-entities []
  (doseq [entity (all-entities)]
    (render-entity entity)))


