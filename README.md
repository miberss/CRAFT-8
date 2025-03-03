# CRAFT-8

CRAFT-8 is a "game engine" server, where you can create games that other players get to play and rate
Games are made with Lua, using implemented API functions, like btn(key) to get the player's current inputs, circ(x,y,color) to create a circle with a color, etc.

You can also make graphical displays (demoscene, and such), like a donut.

https://github.com/user-attachments/assets/24893b41-c991-459b-95b3-1f16a15c3521

CRAFT-8 is meant to be a very rough version of PICO-8 by lexaloffle, and is barely going to "emulate" it. It is not meant to be 1 to 1 compatiable, and you are not able to port current PICO-8 games with lots of tweaking.

As of now, the server that is hosting this project is not setup. However, I am planning for it to release *soon*.

### Here is a list of all of the features CRAFT-8 currently implements, and will on release.

- Custom web code editor.
- Game loading and saving system.
- Lua integration

### Now here is a list of what CRAFT-8 will not (in the forseeable future) support.

- Cross Compatibility with PICO-8, porting from and to CRAFT-8 will not work.
- Virtual Memory, no peeking or any of that.
- 1 to 1 sound implementations. It will use noteblocks
- Using a resourcepack serverside to implement things like sounds and custom textures.


Current supported api
## Graphics
```lua
cls([c]) -- clears screen 
color(c) -- sets draw color in draw state
pset(x,y,[c]) -- pixel set
pget(x,y,[c]) -- pixel get
circfill(x,y,r [,c]) -- filled circle
circ(x,y,r [,c]) -- hollow circle
rect(x1,y1,x2,y2 [,c]) -- hollow rectangle
rectfill(x1,y1,x2,y2 [,c]) -- filled rectangle
line(x1,y1,x2,y2 [,c]) -- line
print(str, [,x] [,y] [,c]) -- prints to screen

-- Not supported, but will soon.

-- this is for drawing a rectangle of pixels from the sprite sheet.
-- sspr( sx, sy, sw, sh, dx, dy, [dw,] [dh,] [flip_x,] [flip_y] )

-- n here is sprite number, sprites are 8x8, they are labeled from 0 to n in the sprite sheet
-- spr( n, [x,] [y,] [w,] [h,] [flip_x,] [flip_y] )
```
## Math
```lua
min(first [,second])
max(first [,second])
mid(first, second, third)
ceil(num)
flr(num)
abs(num)
sgn(num)
sin(num)
cos(num)
atan2(x, y)
rnd( [limit] )
```
## General stuff
```lua
t() = time()
add(table, value)
-- this is being added to, but I haven't had time to figure out the nuances.

btn(input) -- A, S, W, D, Jump, Sprint
```
