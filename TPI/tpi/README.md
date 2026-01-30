# Datos OSRM (osrm-data)

Este README explica cómo descargar y restaurar el contenido de la carpeta `osrm-data` (índices y PBF) que se usa para ejecutar OSRM localmente.

**Enlace de descarga (Google Drive)**
- Pegá aquí el enlace público de Google Drive que contiene el ZIP o la carpeta con `osrm-data`:

  https://drive.google.com/uc?export=download&id=1wO5wU0uJdNsXlMF4kTD-lwd9j5VkXOlx


## Qué contiene
- `argentina-latest.osm.pbf` — archivo fuente OSM (opcional para runtime, necesario para regenerar índices).
- Archivos `argentina-latest.osrm.*` — índices generados por `osrm-extract` / `osrm-contract` (estos son usados por `osrm-routed`).

> IMPORTANTE: El servicio OSRM en `docker-compose.yml` está configurado para usar la carpeta del host `./tpi/osrm-data` y montarla en `/data` dentro del contenedor. El comando que ejecuta el contenedor es `osrm-routed /data/argentina-latest.osrm`.

## Pasos para descargar y restaurar (GUI — navegador)
1. Abrí el enlace de Google Drive que pegaste arriba.
2. Descargá el ZIP o la carpeta completa a tu máquina local.
3. Descomprimí (si venía en ZIP) y copiá el contenido dentro de `D:\Backend De App\TPI\tpi\osrm-data`.
   - Asegurate de que la ruta final contenga archivos como `argentina-latest.osrm.hsgr`, `argentina-latest.osrm.geometry`, etc.

## Pasos para descargar por línea de comandos (opcional)
Si preferís hacerlo por terminal y tenés instalado `gdown` (se puede instalar con `pip install gdown`), podés usarlo para descargar un archivo compartido en Google Drive.

1. Instalá `gdown` si no lo tenés:

```powershell
python -m pip install --user gdown
```

2. Desde PowerShell descargá el archivo (reemplazá `<GDRIVE_URL>` por el link compartido):

```powershell
# Crear carpeta de destino si no existe
New-Item -ItemType Directory -Path 'D:\Backend De App\TPI\tpi\osrm-data' -Force

# Descargar con gdown (si el archivo es un ZIP o tar)
python -m gdown "https://drive.google.com/uc?export=download&id=1wO5wU0uJdNsXlMF4kTD-lwd9j5VkXOlx" -O "D:\\Backend De App\\TPI\\tpi\\osrm-data\\osrm-data.zip"

# Descomprimir si es necesario
Expand-Archive -Path 'D:\Backend De App\TPI\tpi\osrm-data\osrm-data.zip' -DestinationPath 'D:\Backend De App\TPI\tpi\osrm-data' -Force
```

Si no querés instalar `gdown`, también podés descargar el archivo con el navegador y luego copiarlo a la carpeta `tpi/osrm-data`.

## Verificar archivos y permisos
Desde PowerShell comprobá la presencia de los archivos:

```powershell
Get-ChildItem -Path 'D:\Backend De App\TPI\tpi\osrm-data' | Sort-Object Length -Descending | Select-Object Name, @{Name='MB';Expression={[math]::Round($_.Length/1MB,2)}} | Format-Table -AutoSize
```

Deberías ver muchos archivos que empiecen por `argentina-latest.osrm`.

## Reiniciar el contenedor OSRM
Después de copiar los archivos, reiniciá el servicio OSRM para que cargue los índices nuevos:

```powershell
# Desde la raíz del proyecto donde está docker-compose.yml
docker compose -f docker-compose.yml restart osrm

# O para reiniciar todo el stack
docker compose -f docker-compose.yml up -d --build
```
