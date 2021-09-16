(ns game.state
  (:require [game.canvas :as canvas]))

(def TILE-SIZE 16)

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

(def player (atom {:x 2
                   :y 3}))

(defn player-at? [x y]
  (and (= x (:x @player)) (= y (:y @player))))

(defn get-tile [x y]
  (get (:values game-map) (+ x (* (:size game-map) y))))

(defn tile-is? [type x y]
  (= (:type (get-tile x y)) type))

(defn static-movement
  "Can't move! Just returns the same position."
  [monster]
  [(:x monster) (:y monster)])

(defn random-movement
  "Random movement to any surrounding square."
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

(def statue
  {:type :STATUE
   :color "grey"
   :movement static-movement})

(defn create-statue [statue-data]
  (merge statue-data statue))

(def jelly
  {:type :JELLY
   :color "darkred"
   :movement random-movement})

(defn create-jelly [jelly-data]
  (merge jelly-data jelly))

(def monsters (atom  {}))

(defn move-monster [id x y]
  {:type :MOVE_MONSTER :payload {:id id :x x :y y}})

(defn render-monster [monster]
  (canvas/draw-rect
   (* TILE-SIZE (:x monster))
   (* TILE-SIZE (:y monster))
   TILE-SIZE TILE-SIZE
   (:color monster)))


(defn render-monsters []
  (doseq [[_ monster] (seq @monsters)]
    (render-monster monster)))

(defn all-monsters []
  (map last @monsters))

(defn get-monster-at [x y]
  (some
   #(and (= (:x %) x) (= (:y %) y) %)
   (all-monsters)))

(defmulti handle :type)

(defmethod handle :default [action]
  (. js/console error "No action handler defined for: " (:type action)))

(defmethod handle :MOVE_MONSTER [action]
  (swap! monsters conj {(-> action :payload :id) (merge (get @monsters (-> action :payload :id)) {:x (-> action :payload :x)
                                                                                                  :y (-> action :payload :y)})}))

(defn trigger-actions [actions-list]
  (doseq [action actions-list]
    (handle action)))

(defn update-monsters
  "Loop through all monsters and trigger their movement function."
  []
  (doseq [[id monster] (seq @monsters)]
    (let [movement (:movement monster)
          [x y] (movement monster)]
      (trigger-actions [(move-monster id x y)]))))

(defn handle-ai-update
  "Update state for everything not directly controlled by the player."
  []
  (update-monsters))

(def input-keys {38 :UP
                 40 :DOWN
                 37 :LEFT
                 39 :RIGHT})

(defn new-position [x y]
  [(+ (:x @player) x) (+ (:y @player) y)])

(defn move-player [x y] {:type :MOVE_PLAYER :payload {:x x :y y}})

(defmethod handle :MOVE_PLAYER [action]
  (swap! player conj {:x (-> action :payload :x)
                      :y (-> action :payload :y)}))

(defn render-player []
  (canvas/draw-rect
   (* TILE-SIZE (:x @player))
   (* TILE-SIZE (:y @player))
   TILE-SIZE TILE-SIZE
   "green"))



(defn render-tile [tile x y h w]
  (let [colors  {:WALL "black"
                 :BLANK "white"}]
    (canvas/draw-rect x y h w ((:type tile) colors))))

(defn render-map []
  (doseq [[i x] (map-indexed vector (:values game-map))]
    (render-tile
     x
     (* (mod i (:size game-map)) TILE-SIZE)
     (* (Math/floor (/ i (:size game-map))) TILE-SIZE)
     TILE-SIZE
     TILE-SIZE)))

(defn try-move [[x y]]
  (let [monster? (get-monster-at x y)]
    (cond
      monster? nil
      (tile-is? :BLANK x y) (trigger-actions  [(move-player x y)])
      :else nil)))

(defn handle-user-update [key]
  (case key
    :UP (try-move (new-position 0 -1))
    :DOWN (try-move (new-position 0 1))
    :LEFT (try-move (new-position -1 0))
    :RIGHT (try-move (new-position 1 0))
    :default nil))


