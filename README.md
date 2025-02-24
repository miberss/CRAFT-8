## Configuration
1. Copy `config.properties.example` to `config.properties`
2. Fill in your actual API key and other sensitive information
3. The actual `config.properties` file is gitignored and should never be committed

## Graphics
```lua
cls([c]) -- clears screen 
color(c) -- sets draw color in draw state
pset(x,y,[c])
pget(x,y,[c])
circfill(x,y,r [,c]) -- filled circle
circ(x,y,r [,c]) -- hollow circle
rect(x1,y1,x2,y2 [,c]) -- hollow rectangle
rectfill(x1,y1,x2,y2 [,c]) -- filled rectange
line(x1,y1,x2,y2 [,c]) -- line
print(str, [,x] [,y] [,c])
sspr( sx, sy, sw, sh, dx, dy, [dw,] [dh,] [flip_x,] [flip_y] ) -- this is for drawing a rectangle of pixels from the sprite sheet.
spr( n, [x,] [y,] [w,] [h,] [flip_x,] [flip_y] ) -- n here is sprite number, sprites are 8x8, they are labeled from 0 to n in the sprite sheet
```
## Math
```lua
min(first, [second])
max(first, [second])
mid(first, second, third]
ceil, flr, abs, sgn
sin, cos, atan2
rnd() -- random number
```
## General stuff
```lua
t() or time()
add(table, value)
del(table, value)
foreach(table, function)
all(table)

btn(input) -- A, S, W, D, Jump, Sprint
```
