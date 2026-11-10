"""Cria captura ILUSTRATIVA com os assets reais (nao e captura do jogo em execucao)."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont,ImageEnhance
ROOT=Path(__file__).resolve().parents[1]
S=ROOT/'assets'
W,H=920,980
im=Image.new('RGB',(W,H),(8,6,22))
d=ImageDraw.Draw(im)
lines=(S/'maps'/'mapa_v5.txt').read_text().splitlines(); rows=len(lines);cols=len(lines[0]);cell=29.51
bg=Image.open(S/'images'/'mapa_halloween_v5.png').convert('RGB')
mapw=int(cols*cell); maph=int(rows*cell);x0=(W-mapw)//2;y0=62
im.paste(bg.resize((mapw,maph),Image.Resampling.NEAREST),(x0,y0))
d=ImageDraw.Draw(im)
for r,row in enumerate(lines):
 for c,ch in enumerate(row):
  cx=x0+(c+.5)*cell; cy=y0+(r+.5)*cell
  if ch=='.':
   rr=4.1;d.ellipse((cx-rr,cy-rr,cx+rr,cy+rr),fill=(255,213,120))
  elif ch=='p':
   rr=9;d.ellipse((cx-rr-3,cy-rr-3,cx+rr+3,cy+rr+3),fill=(86,25,43))
   d.ellipse((cx-rr,cy-rr,cx+rr,cy+rr),fill=(248,54,85))
   d.ellipse((cx-3,cy-6,cx+1,cy-2),fill=(255,211,180))
  elif ch in 'PRLVA':
   if ch=='P':tex=Image.open(S/'images'/'pacman_v5.png').convert('RGBA').crop((0,0,48,48)); size=48
   else:
    paths={'R':S/'sprites'/'fantasmas'/'pacman_halloween_ghost_pack'/'fantasma_bruxa_spritesheet.png',
      'L':S/'images'/'fantasma_bruxa_laranja.png','V':S/'images'/'fantasma_bruxa_verde.png','A':S/'images'/'fantasma_bruxa_azul.png'}
    tex=Image.open(paths[ch]).convert('RGBA').crop((0,0,32,32));size=40
   tex=tex.resize((size,size),Image.Resampling.NEAREST)
   im.paste(tex,(int(cx-size/2),int(cy-size/2)),tex)
  elif ch=='F':
   tex=Image.open(S/'images'/'cereja.png').convert('RGBA').resize((37,37),Image.Resampling.NEAREST)
   im.paste(tex,(int(cx-18),int(cy-18)),tex)
d=ImageDraw.Draw(im)
d.rectangle((0,0,W,58),fill=(20,9,34));d.rectangle((0,929,W,H),fill=(20,9,34))
fontdir=Path('/usr/share/fonts/truetype/dejavu')
large=ImageFont.truetype(str(fontdir/'DejaVuSansMono-Bold.ttf'),20)
mid=ImageFont.truetype(str(fontdir/'DejaVuSansMono.ttf'),14)
d.text((22,5),'PAC-MAN  HALLOWEEN  V5',font=large,fill=(255,164,76))
d.text((22,34),'PONTOS  0000        RECORDE  0000',font=mid,fill=(243,231,255))
d.text((760,18),'VIDAS  3',font=mid,fill=(243,231,255))
d.text((23,947),'SETAS / WASD   |   R: REINICIAR   |   163 ITENS',font=mid,fill=(195,169,235))
output=ROOT/'PREVIA_PACMAN_HALLOWEEN_V5.png'
im.save(output,optimize=True)
print(output)
