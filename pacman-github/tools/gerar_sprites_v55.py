"""Cria apenas novos sprites V5.5. Nao altera o mapa ou os arquivos da V5.
Usa pixels RGBA e mesma grade de 48 px (Pac-Man) e 32 px (fantasmas).
"""
from pathlib import Path
from PIL import Image, ImageDraw
import colorsys
ROOT=Path(__file__).resolve().parents[1]
IMAGES=ROOT/'assets/images'
S=48
original=Image.open(IMAGES/'pacman_monstro_v5.png').convert('RGBA')
novo=Image.new('RGBA',original.size,(0,0,0,0))
for direcao in range(4):
 for frame in range(4):
  # Cria o corpo a partir do monstruoso V5, apagando o chapeu no quadro
  # e reconstruindo o topo em pixel art (olhos e boca permanecem animados).
  fatia=original.crop((frame*S,direcao*S,(frame+1)*S,(direcao+1)*S))
  p=ImageDraw.Draw(fatia)
  p.rectangle((10,0,38,21),fill=(0,0,0,0))
  # Cabelo flamejante, couro e chifres expostos no lugar do chapeu.
  p.polygon([(10,22),(8,14),(13,8),(17,15),(21,11),(24,15),(29,10),(33,15),(37,9),(41,17),(38,25)],fill=(108,15,56))
  p.polygon([(12,21),(11,15),(16,12),(18,19)],fill=(255,116,39))
  p.polygon([(31,19),(34,12),(38,15),(37,24)],fill=(255,130,43))
  p.polygon([(13,15),(10,3),(17,8),(20,18)], fill=(236,95,40))
  p.polygon([(34,16),(38,2),(29,9),(28,18)], fill=(243,81,38))
  p.line((15,14,13,6),fill=(255,189,82),width=2)
  p.line((34,13,37,5),fill=(255,177,61),width=2)
  p.ellipse((14,17,34,29),fill=(212,42,44))
  p.polygon([(15,20),(21,17),(21,23),(15,25)],fill=(255,242,186))
  p.polygon([(26,17),(33,20),(33,25),(26,23)],fill=(255,242,186))
  p.rectangle((18,19,21,22),fill=(149,8,30))
  p.rectangle((29,19,32,22),fill=(149,8,30))
  p.line((17,15,22,17),fill=(30,5,35),width=2)
  p.line((27,17,33,15),fill=(30,5,35),width=2)
  # Preserva os dentes de baixo visiveis sob a boca aberta.
  p.polygon([(15,29),(19,29),(17,35)],fill=(255,244,193))
  p.polygon([(31,29),(35,29),(33,35)],fill=(255,244,193))
  novo.alpha_composite(fatia,(frame*S,direcao*S))
novo.save(IMAGES/'pacman_monstro_v55.png',optimize=True)
# Chapéu isolado: flutua, gira e cai no momento da transformacao.
chapeu=Image.new('RGBA',(48,48),(0,0,0,0))
p=ImageDraw.Draw(chapeu)
p.polygon([(8,29),(19,5),(27,8),(37,29)],fill=(78,31,133),outline=(33,12,64))
p.polygon([(18,8),(26,10),(24,22),(12,28)],fill=(160,72,210))
p.rectangle((7,27,39,33),fill=(50,18,81))
p.rectangle((12,29,35,32),fill=(146,65,187))
p.rectangle((22,28,29,32),fill=(255,184,58))
p.line((13,10,20,8),fill=(217,135,247),width=2)
chapeu.save(IMAGES/'chapeu_caindo_v55.png',optimize=True)
# Mais dois espectros: magenta e dourado. Mantem o formato sprite de 4 x 4.
fonte=Image.open(IMAGES/'fantasma_bruxa_azul.png').convert('RGBA')
for name,novoHue in [('fantasma_bruxa_rosa_v55.png',.91),('fantasma_bruxa_dourado_v55.png',.116)]:
 out=Image.new('RGBA',fonte.size,(0,0,0,0))
 pixelsIn=fonte.load(); pixelsOut=out.load()
 for y in range(fonte.height):
  for x in range(fonte.width):
   r,g,b,a=pixelsIn[x,y]
   if a==0:continue
   h,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
   # Olhos/brancos e areas pouco saturadas sao preservados.
   if s>.35 and v>.13 and not (0.08<h<.19 and r>150):
    rr,gg,bb=colorsys.hsv_to_rgb(novoHue,min(1,s*.9+0.08),v)
    pixelsOut[x,y]=(int(rr*255),int(gg*255),int(bb*255),a)
   else:pixelsOut[x,y]=(r,g,b,a)
 out.save(IMAGES/name,optimize=True)
# Fantasma assustado: brilho ciano e pupilas preocupadas, colar ondulado.
fear=Image.new('RGBA',fonte.size,(0,0,0,0))
for row in range(4):
 for col in range(4):
  tile=fonte.crop((col*32,row*32,(col+1)*32,(row+1)*32))
  ip=tile.load()
  for y in range(32):
   for x in range(32):
    r,g,b,a=ip[x,y]
    if a==0:continue
    h,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
    if s>.31 and v>.12:
     rr,gg,bb=colorsys.hsv_to_rgb(.54,.70,min(1,v*1.18+.10))
     ip[x,y]=(int(rr*255),int(gg*255),int(bb*255),a)
  p=ImageDraw.Draw(tile)
  # As primeiras linhas sao os estados com cabeca completa do fantasma.
  if row in (0,1):
   p.line((9,14,12,12),fill=(34,34,83),width=1)
   p.line((20,12,23,14),fill=(34,34,83),width=1)
   p.ellipse((14,21,18,25),fill=(23,49,90))
  fear.alpha_composite(tile,(col*32,row*32))
fear.save(IMAGES/'fantasma_assustado_v55.png',optimize=True)
print('Sprites V5.5 gerados:', [p.name for p in IMAGES.glob('*v55*')])
