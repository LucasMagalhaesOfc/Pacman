"""Previa ILUSTRATIVA V5.5 usando assets do pacote. Nao e captura real de gameplay."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'assets'
W,H=1450,980
im=Image.new('RGB',(W,H),(11,6,25))
d=ImageDraw.Draw(im)
d.rectangle((0,0,920,H),fill=(8,6,22))
linhas=(A/'maps/mapa_v5.txt').read_text().splitlines()
h=len(linhas);w=len(linhas[0]);escala=29.5
mapa=Image.open(A/'images/mapa_halloween_v5.png').convert('RGB')
ml=round(w*escala);mh=round(h*escala);x0=(920-ml)//2;y0=62
im.paste(mapa.resize((ml,mh),Image.Resampling.NEAREST),(x0,y0))
d=ImageDraw.Draw(im)
icons={ 'R':A/'sprites/fantasmas/pacman_halloween_ghost_pack/fantasma_bruxa_spritesheet.png',
'L':A/'images/fantasma_bruxa_laranja.png','V':A/'images/fantasma_bruxa_verde.png',
'A':A/'images/fantasma_bruxa_azul.png','S':A/'images/fantasma_bruxa_rosa_v55.png',
'D':A/'images/fantasma_bruxa_dourado_v55.png'}
for r,row in enumerate(linhas):
 for c,ch in enumerate(row):
  cx=x0+(c+.5)*escala;cy=y0+(r+.5)*escala
  if ch=='.':
   rad=3.9;d.ellipse((cx-rad,cy-rad,cx+rad,cy+rad),fill=(255,224,133))
  elif ch=='p':
   rad=9;d.ellipse((cx-rad-4,cy-rad-4,cx+rad+4,cy+rad+4),fill=(80,20,45))
   d.ellipse((cx-rad,cy-rad,cx+rad,cy+rad),fill=(252,38,74))
  elif ch=='P':
   img=Image.open(A/'images/pacman_monstro_v55.png').convert('RGBA').crop((0,0,48,48))
   img=img.resize((54,54),Image.Resampling.NEAREST)
   im.paste(img,(int(cx-27),int(cy-27)),img)
   hat=Image.open(A/'images/chapeu_caindo_v55.png').convert('RGBA').resize((34,34),Image.Resampling.NEAREST)
   hat=hat.rotate(-35,expand=True,resample=Image.Resampling.NEAREST)
   im.paste(hat,(int(cx+24),int(cy+10)),hat)
  elif ch in icons:
   # Ilustra a fuga: visual assustado no lugar de alguns fantasmas.
   path=A/'images/fantasma_assustado_v55.png' if ch in 'RAS' else icons[ch]
   img=Image.open(path).convert('RGBA').crop((0,0,32,32)).resize((42,42),Image.Resampling.NEAREST)
   im.paste(img,(int(cx-21),int(cy-21)),img)
  elif ch=='F':
   img=Image.open(A/'images/cereja.png').convert('RGBA').resize((37,37),Image.Resampling.NEAREST)
   im.paste(img,(int(cx-18),int(cy-18)),img)
d=ImageDraw.Draw(im)
fdir=Path('/usr/share/fonts/truetype/dejavu')
font=lambda size,bold=False:ImageFont.truetype(str(fdir/('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')),size)
f24=font(24,True);f17=font(17);f14=font(14);f31=font(31,True)
d.rectangle((0,0,920,57),fill=(28,12,42));d.rectangle((0,928,920,H),fill=(28,12,42))
d.text((22,11),'PAC-MAN HALLOWEEN   V5.5',font=f24,fill=(255,167,65))
d.text((22,936),'PONTOS: 1450     TEMPO: 02:14     P: PAUSA',font=f17,fill=(221,200,248))
d.rectangle((920,0,1450,980),fill=(22,11,36))
d.line((923,0,923,H),fill=(226,116,40),width=5)
d.text((968,30),'HALLOWEEN',font=f31,fill=(255,183,74))
d.text((968,73),'PAC-MAN 5.5',font=f31,fill=(220,161,255))
d.text((970,133),'6 fantasmas  /  FUGA NO MODO MONSTRO',font=f14,fill=(222,205,245))

def mostrar(nome,path,x,y,size=110,frame=48):
 img=Image.open(A/path).convert('RGBA').crop((0,0,frame,frame))
 img=img.resize((size,size),Image.Resampling.NEAREST)
 im.paste(img,(x,y),img)
 d.text((x,y+size+8),nome,font=f14,fill=(255,236,201))
mostrar('NORMAL','images/pacman_v5.png',981,187)
mostrar('MONSTRO','images/pacman_monstro_v55.png',1185,187)
mostrar('MEDO','images/fantasma_assustado_v55.png',981,360,frame=32)
mostrar('ROSA','images/fantasma_bruxa_rosa_v55.png',1185,360,frame=32)
d.rectangle((959,560,1415,918),fill=(38,24,56),outline=(114,67,157),width=2)
d.text((989,588),'RANKING LOCAL',font=f24,fill=(255,175,78))
d.text((988,649),'POS   NOME       PONTOS   TEMPO',font=f14,fill=(222,196,247))
d.line((988,680,1386,680),fill=(118,83,154),width=2)
d.text((990,709),'Cada vitoria salva:',font=f17,fill=(248,239,255))
d.text((990,745),'  - Nome do jogador',font=f17,fill=(226,204,247))
d.text((990,780),'  - Pontuacao',font=f17,fill=(226,204,247))
d.text((990,815),'  - Tempo para vencer',font=f17,fill=(226,204,247))
d.text((991,874),'ORDENADO POR PONTOS / MINUTO',font=f14,fill=(249,178,89))
d.text((960,948),'PREVIA ILUSTRATIVA - NAO E TELA REAL',font=f14,fill=(158,141,174))
out=ROOT/'PREVIA_PACMAN_HALLOWEEN_V55.png'
im.save(out,optimize=True)
print(out)
