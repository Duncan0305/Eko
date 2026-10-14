param([string]$OutputDirectory = (Join-Path $PSScriptRoot '../.tools/fixtures'))

$ErrorActionPreference = 'Stop'
$ffmpeg = (Get-Command ffmpeg -ErrorAction Stop).Source
New-Item -ItemType Directory -Force -Path $OutputDirectory, (Join-Path $OutputDirectory 'Album') | Out-Null

# Test-only sine tones: these files are never bundled into the app.
& $ffmpeg -hide_banner -loglevel error -f lavfi -i 'sine=frequency=440:duration=2' `
    -metadata title='Amanecer' -metadata artist='EKO Test' -metadata album='Prueba local' `
    -metadata genre='Ambient' -metadata date='2026' -metadata track='1/3' `
    -c:a libmp3lame -y (Join-Path $OutputDirectory '01-amanecer.mp3')
if ($LASTEXITCODE -ne 0) { throw 'Could not create tagged MP3 fixture' }

& $ffmpeg -hide_banner -loglevel error -f lavfi -i 'sine=frequency=330:duration=3' `
    -c:a pcm_s16le -y (Join-Path $OutputDirectory 'Album/sin-etiquetas.wav')
if ($LASTEXITCODE -ne 0) { throw 'Could not create WAV fixture' }

& $ffmpeg -hide_banner -loglevel error -f lavfi -i 'color=c=0x00D4C7:s=256x256' `
    -vf 'drawbox=x=32:y=32:w=192:h=192:color=0x0B0F14:t=fill,drawbox=x=64:y=64:w=128:h=32:color=0x00E676:t=fill,drawbox=x=64:y=112:w=96:h=32:color=0x00D4C7:t=fill,drawbox=x=64:y=160:w=128:h=32:color=0x00B0FF:t=fill' `
    -frames:v 1 -y (Join-Path $OutputDirectory 'cover.jpg')
if ($LASTEXITCODE -ne 0) { throw 'Could not create artwork fixture' }

& $ffmpeg -hide_banner -loglevel error -i (Join-Path $OutputDirectory '01-amanecer.mp3') `
    -i (Join-Path $OutputDirectory 'cover.jpg') -map 0:a -map 1:v -c copy -id3v2_version 3 `
    -metadata title='Con caratula' -metadata:s:v title='Album cover' -metadata:s:v comment='Cover (front)' `
    -disposition:v attached_pic -y (Join-Path $OutputDirectory 'Album/con-caratula.mp3')
if ($LASTEXITCODE -ne 0) { throw 'Could not embed artwork fixture' }

'invalid audio fixture' | Set-Content (Join-Path $OutputDirectory 'roto.mp3')
'non audio' | Set-Content (Join-Path $OutputDirectory 'notas.txt')
Write-Output "Test fixtures created in $OutputDirectory"
