(ns game.state
  (:require
   [reagent.core :as r]
   [game.canvas :as canvas]
   [game.config :as config]))

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

(def player (r/atom {:x 2
                     :y 3
                     :health 100
                     :direction :UP}))

(def monsters (r/atom  {}))

(def events (r/atom '()))

(defn create-statue [statue-data]
  (merge statue-data config/statue))

(defn create-jelly [jelly-data]
  (merge jelly-data config/jelly))

;;;; State Queries?
(defn in? [coll element]
  (some #(= element %) coll))

(defn get-adjacent-squares [x y]
  [[x (inc y)] [x (dec y)]
   [(inc x) y] [(dec x) y]])

(defn player-at? [x y]
  (and (= x (:x @player)) (= y (:y @player))))

(defn player-adjacent? [x y]
  (let [adjacent-squares (get-adjacent-squares x y)]
    (in? adjacent-squares [(:x @player) (:y @player)])))

(defn get-player-tile-infront []
  (let [direction (:direction @player)
        x (:x @player)
        y (:y @player)]
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

(defn all-monsters []
  (map last @monsters))

(defn get-monster-at [x y]
  (some
   #(and (= (:x %) x) (= (:y %) y) %)
   (all-monsters)))

;;;; Monster Movement
(defmulti movement :movement)

(defmethod movement :STATIC
  [monster]
  [(:x monster) (:y monster)])

(defmethod movement :RANDOM
  [monster]
  (let [x (:x monster)
        y (:y monster)
        choices (get-adjacent-squares x y)
        [x2 y2] (get choices (rand-int 4))]
    (cond
      (player-at? x2 y2) [x y]
      (tile-is? :BLANK x2 y2) [x2 y2]
      :else [x y])))


;;;; State Mutations
(defn add-event [text]
  (swap! events conj text))

(defn move-player [x y]
  (swap! player conj {:x x
                      :y y}))

(defn set-direction [direction]
  (swap! player conj {:direction direction}))

(defn kill-player []
  (reset! game-state :GAME_OVER))

(defn hurt-player [amount]
  (swap! player update-in [:health] - amount)
  (when (<= (:health @player) 0) (kill-player)))

(defn kill-monster [monster-id]
  (swap! monsters dissoc monster-id)
  (add-event "Player killed monster"))

(defn hurt-monster [monster-id amount]
  (swap! monsters update-in [monster-id :health] - amount))

(defn move-monster [monster-id x y]
  (swap! monsters conj {monster-id (merge (get @monsters monster-id) {:x x
                                                                      :y y})}))

(defn coordinates->i [size x y]
  (+ x (* size y)))

(defn open-door [x y]
  (add-event (str "Opened door"))
  (swap! game-map update-in [:values] (fn [tiles] (assoc tiles (coordinates->i (:size @game-map) x y) (blank)))))

(defn attack-monster [monster-id]
  (let [monster-health (-> @monsters monster-id :health)
        damage 5
        new-health (- monster-health damage)]
    (add-event "Player attacked monster")
    (cond
      (<= new-health 0) (kill-monster monster-id)
      :else (hurt-monster monster-id damage))))

(defn attack-player [monster]
  (add-event "Monster attacks player")
  (hurt-player (:attack monster)))

;;;; Logic
(defn update-monsters
  "Loop through all monsters and trigger their movement function."
  []
  (doseq [[id monster] (seq @monsters)]
    (cond
      (player-adjacent? (:x monster) (:y monster)) (attack-player monster)
      :else (let [[x y] (movement monster)]
              (move-monster id x y)))))

(defn new-position [x y]
  [(+ (:x @player) x) (+ (:y @player) y)])

(defn try-move [[x y] direction]
  (let [monster (get-monster-at x y)]
    (set-direction direction)
    (cond
      (some? monster) (attack-monster (:id monster))
      (tile-is? :BLANK x y) (move-player x y)
      :else nil)))

(defn try-open-door []
  (let [[x y] (get-player-tile-infront)]
    (when (tile-is? :DOOR x y)
      (open-door x y))))

;;; INPUT
(defn handle-user-update [key]
  (case key
    :UP (try-move (new-position 0 -1) :UP)
    :DOWN (try-move (new-position 0 1) :DOWN)
    :LEFT (try-move (new-position -1 0) :LEFT)
    :RIGHT (try-move (new-position 1 0) :RIGHT)
    :O (try-open-door)
    :default nil))

;;;; RENDERING
(defn i->coordinates [i]
  [(mod i (:size @game-map)) (Math/floor (/ i (:size @game-map)))])

(defn coordinates->pixels [[x y]]
  [(* x config/TILE-SIZE) (* y config/TILE-SIZE)])

(defmulti render-tile (fn [tile _] (:type tile)))
(defmethod render-tile :DOOR [_ coordinates]
  (let [[x y] (coordinates->pixels coordinates)]
    (canvas/draw-rect x y config/TILE-SIZE config/TILE-SIZE "orange")))

(defmethod render-tile :default [tile coordinates]
  (let [[x y] (coordinates->pixels coordinates)]
    (canvas/draw-rect x y config/TILE-SIZE config/TILE-SIZE (:color tile))))

(defn render-map []
  (doseq [[i tile] (map-indexed vector (:values @game-map))]
    (let [coordinates (i->coordinates i)]
      (render-tile
       tile
       coordinates))))

(defn render-player []
  (canvas/draw-rect
   (* config/TILE-SIZE (:x @player))
   (* config/TILE-SIZE (:y @player))
   config/TILE-SIZE config/TILE-SIZE
   "green"))

(defn render-monster [monster]
  (canvas/draw-rect
   (* config/TILE-SIZE (:x monster))
   (* config/TILE-SIZE (:y monster))
   config/TILE-SIZE config/TILE-SIZE
   (:color monster)))


(defn render-monsters []
  (doseq [[_ monster] (seq @monsters)]
    (render-monster monster)))


