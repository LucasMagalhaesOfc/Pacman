"""Gera o labirinto e assets pixel art da V5 (somente Pillow). Nao roda no jogo.
A grade TXT e a fonte unica de verdade das paredes e dos coletaveis.
"""
from pathlib import Path
import random, math
from PIL import Image, ImageDraw
BASE = Path(__file__).resolve().parents[1]
ASSETS = BASE/'assets'; (ASSETS/'maps').mkdir(exist_ok=True); (ASSETS/'images').mkdir(exist_ok=True)
RNG=random.Random(20261109)
W,H=25,29
board=[['#']*W for _ in range(H)]
for r in range(1,H,2):
 for c in range(1,W,2): board[r][c]=' '
# Uma malha de corredores de uma celula, com loops reais (nao ha corredores paralelos).
visited={(1,1)}; stack=[(1,1)]
while stack:
 r,c=stack[-1]; directions=[(2,0),(-2,0),(0,2),(0,-2)]; RNG.shuffle(directions)
 choices=[(r+dr,c+dc,dr,dc) for dr,dc in directions if 1<=r+dr<H-1 and 1<=c+dc<W-1 and (r+dr,c+dc) not in visited]
 if choices:
  nr,nc,dr,dc=choices[0]; board[r+dr//2][c+dc//2]=' '; visited.add((nr,nc)); stack.append((nr,nc))
 else:stack.pop()
# Abre conexoes extras: voltas/rotas alternativas sem criar areas largas (even/even permanecem paredes).
connections=[]
for r in range(1,H-1):
 for c in range(1,W-1):
  if (r%2==0) != (c%2==0) and board[r][c]=='#':
   if r%2==0 and board[r-1][c]==' ' and board[r+1][c]==' ':connections.append((r,c))
   if c%2==0 and board[r][c-1]==' ' and board[r][c+1]==' ':connections.append((r,c))
RNG.shuffle(connections)
for r,c in connections[:int(len(connections)*.34)]:board[r][c]=' '
# Evita uma trilha comprida sem saidas perto do centro; espacos continuam de 1 celula.
# Itens em UM eixo central, distantes dois tiles ao longo do percurso.
for r in range(1,H-1):
 for c in range(1,W-1):
  if board[r][c]==' ' and (r+c)%2==0: board[r][c]='.'
# Pontos: 1 por celula logica aberta, nenhum 2x2 de piso. Marcadores sao consumidos no parse.
positions={'P':(27,13),'R':(13,5),'L':(13,19),'V':(7,13),'A':(21,13),'S':(5,9),'D':(23,19),
           'p':[(3,3),(3,21),(25,3),(25,21)], 'F':(19,11)}
for ch,pos in positions.items():
 if ch=='p':
  for r,c in pos: assert board[r][c]!='#';board[r][c]='p'
 else:
  r,c=pos;assert board[r][c]!='#',(ch,r,c);board[r][c]=ch
text='\n'.join(''.join(row) for row in board)+'\n'
(ASSETS/'maps'/'mapa_v5.txt').write_text(text,encoding='UTF-8')
print('Mapa com 6 spawns (V5.5):'); print(text); print('Coletaveis:',text.count('.')+text.count('p')+text.count('F'))
# Fundo 32px por celula. A geometria vem do TXT, jamais dos pixels deste PNG.
T=32; BG=(10,9,23); Wall=(39,19,62)
im=Image.new('RGB',(W*T,H*T),BG); d=ImageDraw.Draw(im)
for r in range(H):
 for c in range(W):
  x,y=c*T,r*T; ch=board[r][c]
  if ch=='#':
   d.rectangle((x,y,x+31,y+31),fill=(35,18,54))
   # Blocos de pedra / textura de alvenaria.
   d.line((x+2,y+9,x+29,y+9),fill=(48,24,72),width=1)
   d.line((x+2,y+22,x+29,y+22),fill=(48,24,72),width=1)
   d.line((x+15,y+1,x+15,y+8),fill=(56,28,81),width=1)
   d.line((x+8,y+10,x+8,y+21),fill=(56,28,81),width=1)
   d.line((x+21,y+23,x+21,y+31),fill=(56,28,81),width=1)
   # Contorno neon apenas onde existe corredor, sem multicores em toda parte.
   for dr,dc in ((-1,0),(1,0),(0,-1),(0,1)):
    nr,nc=r+dr,c+dc
    if 0<=nr<H and 0<=nc<W and board[nr][nc]!='#':
     if dr==-1:
      d.rectangle((x+2,y+1,x+29,y+3),fill=(155,78,217))
      d.line((x+4,y+4,x+27,y+4),fill=(219,108,63))
     if dr==1:
      d.rectangle((x+2,y+28,x+29,y+30),fill=(155,78,217))
      d.line((x+4,y+27,x+27,y+27),fill=(219,108,63))
     if dc==-1:
      d.rectangle((x+1,y+2,x+3,y+29),fill=(155,78,217))
      d.line((x+4,y+4,x+4,y+27),fill=(219,108,63))
     if dc==1:
      d.rectangle((x+28,y+2,x+30,y+29),fill=(155,78,217))
      d.line((x+27,y+4,x+27,y+27),fill=(219,108,63))
  else:
   d.rectangle((x,y,x+31,y+31),fill=(12,11,29))
   if RNG.random()<.26:
    xx=x+RNG.randrange(4,29); yy=y+RNG.randrange(4,29)
    d.rectangle((xx,yy,xx+1,yy+1),fill=(33,25,52))
# Pequenos simbolos de Halloween APENAS nos blocos de parede (nao obstruem pontos).
def pumpkin(x,y):
 d.ellipse((x-9,y-6,x+9,y+7),fill=(216,92,24),outline=(255,150,49),width=1)
 d.line((x,y-8,x+2,y-5),fill=(86,176,79),width=3)
 d.polygon([(x-5,y-2),(x-2,y-2),(x-3,y+1)],fill=(17,13,33))
 d.polygon([(x+3,y-2),(x+6,y-2),(x+5,y+1)],fill=(17,13,33))
 d.line((x-3,y+3,x+3,y+3),fill=(17,13,33),width=2)
def gravestone(x,y):
 d.rounded_rectangle((x-8,y-9,x+8,y+10),radius=5,fill=(94,89,119),outline=(145,136,179),width=1)
 d.line((x-4,y-2,x+4,y-2),fill=(53,48,77),width=2)
 d.line((x,y-5,x,y+3),fill=(53,48,77),width=2)
for r,c in [(0,3),(0,21),(28,3),(28,21),(14,0),(14,24)]:
 if board[r][c]=='#':pumpkin(c*T+16,r*T+16)
for r,c in [(6,0),(6,24),(22,0),(22,24)]:
 if board[r][c]=='#':gravestone(c*T+16,r*T+16)
im.save(ASSETS/'images'/'mapa_halloween_v5.png',optimize=True)
# Spritesheets 4 frames boca x 4 direcoes: 0 direita, 1 esquerda, 2 cima, 3 baixo.
# Personagem e chapéu continuam voltados para cima (sem rotacionar o chapéu).
SZ=48
def make_sprite(monster, direction, frame):
 s=4; canv=Image.new('RGBA',(SZ,SZ),(0,0,0,0)); p=ImageDraw.Draw(canv)
 # sombra e halo
 p.ellipse((9,39,39,43),fill=(21,7,42,135))
 if monster:
  p.ellipse((7,11,41,42),fill=(255,81,35,70))
  p.polygon([(10,20),(6,14),(12,13),(16,19)],fill=(255,94,32))
  p.polygon([(32,19),(36,12),(41,15),(37,24)],fill=(255,94,32))
  p.ellipse((9,15,39,42),fill=(59,10,43),outline=(255,91,46),width=2)
  p.ellipse((12,16,36,39),fill=(209,43,35))
  p.ellipse((16,17,34,32),fill=(251,98,36))
  p.arc((10,16,39,41),10,180,fill=(255,151,46),width=2)
 else:
  p.ellipse((9,15,39,42),fill=(109,48,9),outline=(89,38,10),width=1)
  p.ellipse((10,14,38,40),fill=(255,194,47))
  p.ellipse((12,16,33,34),fill=(255,225,74))
  p.arc((10,15,38,40),50,205,fill=(255,150,35),width=2)
 # Boca: abertura cresce frames 0,1,2,3; direcao lateral/vertical com forma apropriada.
 openness=[2,5,8,5][frame]
 mouth=(28,28)
 if direction==0: points=[(25,28),(41,28-openness),(41,28+openness)]
 elif direction==1: points=[(23,28),(7,28-openness),(7,28+openness)]
 elif direction==2: points=[(24,27),(24-openness,12),(24+openness,12)]
 else:points=[(24,29),(24-openness,43),(24+openness,43)]
 p.polygon(points,fill=(23,8,35) if monster else (22,15,29))
 if monster:
  # dentes pontiagudos nas laterais da mordida
  if direction in (0,1):
   baseX=31 if direction==0 else 17
   for yy in [25,31]:
    p.polygon([(baseX-3,yy-3),(baseX+3,yy-3),(baseX,yy+2)], fill=(255,238,194))
  else:
   p.polygon([(19,28),(22,28),(20,34)],fill=(255,238,194))
 # Olhos e sobrancelhas diferem nos dois modos.
 if direction==0:eyeX=25
 elif direction==1:eyeX=19
 else:eyeX=24
 if monster:
  p.rectangle((eyeX-5,19,eyeX+3,24),fill=(255,243,179))
  p.rectangle((eyeX,20,eyeX+3,23),fill=(103,14,31))
  p.line((eyeX-5,18,eyeX+4,21),fill=(43,13,49),width=3)
 else:
  p.ellipse((eyeX-4,18,eyeX+4,26),fill=(251,252,245))
  p.rectangle((eyeX,20,eyeX+2,25),fill=(39,24,57))
  p.point((eyeX+1,20),fill=(255,255,255))
 # Feiticeiro: chapéu bem desenhado, sempre vertical.
 p.polygon([(13,17),(22,3),(27,5),(35,17)],fill=(65,29,108),outline=(24,11,39))
 p.polygon([(21,4),(26,7),(24,14),(17,16)],fill=(129,65,193))
 p.rectangle((14,16,35,19),fill=(35,16,54))
 p.rectangle((15,18,34,21),fill=(101,45,155))
 p.rectangle((22,17,28,20),fill=(247,166,51))
 p.rectangle((24,18,26,19),fill=(253,225,133))
 p.line((17,6,24,5),fill=(188,132,224),width=1)
 if monster:
  p.line((11,19,16,17),fill=(255,110,45),width=2)
  p.line((33,18,38,20),fill=(255,110,45),width=2)
 else:
  p.point((15,31),fill=(255,248,171))
 return canv
for mon,name in [(False,'pacman_v5.png'),(True,'pacman_monstro_v5.png')]:
 sheet=Image.new('RGBA',(SZ*4,SZ*4),(0,0,0,0))
 for row in range(4):
  for col in range(4):sheet.alpha_composite(make_sprite(mon,row,col),(col*SZ,row*SZ))
 sheet.save(ASSETS/'images'/name,optimize=True)
print('Assets gerados.')
