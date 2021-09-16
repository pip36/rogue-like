(ns game.state
  (:require
   [reagent.core :as r]
   [game.canvas :as canvas]
   [game.config :as config]))

;;;; STATE DATA
(defn wall []
  {:type :WALL})

(defn blank []
  {:type :BLANK})

(def game-map
  {:size 15
   :values [(wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (wall) (wall) (wall) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (blank) (wall) (blank) (blank) (blank) (wall)
            (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall) (wall) (wall)  (wall)  (wall)  (wall)]})

(def player (r/atom {:x 2
                     :y 3}))

(def monsters (r/atom  {}))

(defn create-statue [statue-data]
  (merge statue-data config/statue))

(defn create-jelly [jelly-data]
  (merge jelly-data config/jelly))

;;;; State Queries?

(defn player-at? [x y]
  (and (= x (:x @player)) (= y (:y @player))))

(defn get-tile [x y]
  (get (:values game-map) (+ x (* (:size game-map) y))))

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
        choices [[x (inc y)] [x (dec y)]
                 [(inc x) y] [(dec x) y]]
        [x2 y2] (get choices (rand-int 4))]
    (cond
      (player-at? x2 y2) [x y]
      (tile-is? :BLANK x2 y2) [x2 y2]
      :else [x y])))


;;;; State Mutations
(defn move-player [x y]
  (swap! player conj {:x x
                      :y y}))

(defn kill-monster [monster-id]
  (swap! monsters dissoc monster-id))

(defn hurt-monster [monster-id amount]
  (swap! monsters update-in [monster-id :health] - amount))

(defn move-monster [monster-id x y]
  (swap! monsters conj {monster-id (merge (get @monsters monster-id) {:x x
                                                                      :y y})}))

(defn attack-monster [monster-id]
  (let [monster-health (-> @monsters monster-id :health)
        damage 5
        new-health (- monster-health damage)]
    (cond
      (<= new-health 0) (kill-monster monster-id)
      :else (hurt-monster monster-id damage))))

;;;; Logic
(defn update-monsters
  "Loop through all monsters and trigger their movement function."
  []
  (doseq [[id monster] (seq @monsters)]
    (let [[x y] (movement monster)]
      (move-monster id x y))))

(defn new-position [x y]
  [(+ (:x @player) x) (+ (:y @player) y)])

(defn try-move [[x y]]
  (let [monster (get-monster-at x y)]
    (cond
      (some? monster) (attack-monster (:id monster))
      (tile-is? :BLANK x y) (move-player x y)
      :else nil)))

;;; INPUT
(defn handle-user-update [key]
  (case key
    :UP (try-move (new-position 0 -1))
    :DOWN (try-move (new-position 0 1))
    :LEFT (try-move (new-position -1 0))
    :RIGHT (try-move (new-position 1 0))
    :default nil))

;;;; RENDERING
(defn render-tile [tile x y h w]
  (let [colors  {:WALL "black"
                 :BLANK "white"}]
    (canvas/draw-rect x y h w ((:type tile) colors))))

(defn render-map []
  (doseq [[i x] (map-indexed vector (:values game-map))]
    (render-tile
     x
     (* (mod i (:size game-map)) config/TILE-SIZE)
     (* (Math/floor (/ i (:size game-map))) config/TILE-SIZE)
     config/TILE-SIZE
     config/TILE-SIZE)))

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


