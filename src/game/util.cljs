(ns game.util)

(defn player?
  "Checks if entity is a player."
  [entity]
  (= :player (:id entity)))

(defn monster?
  "Checks if entity is a monster"
  [entity]
  (and (some? entity) (not (player? entity))))

(defn in?
  "Checks if collection contains element"
  [coll element]
  (some #(= element %) coll))

(defn adjacent-squares
  "Given coordinates, returns coordinates for adjacent squares.
   
   |_|X|_|
   |X|P|X|
   |_|X|_|
   "
  [x y]
  [[x (inc y)] [x (dec y)]
   [(inc x) y] [(dec x) y]])

(defn coordinates->i
  "For given size of tilemap, convert coordinates into an index"
  [size x y]
  (+ x (* size y)))

(defn i->coordinates
  "For given size of tilemap, convert index into coordinates"
  [size i]
  [(mod i size) (Math/floor (/ i size))])

(defn add-coordinates
  "Returns sum of 2 coordinates"
  [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn clamp [x min max]
  (Math/min (Math/max x min) max))