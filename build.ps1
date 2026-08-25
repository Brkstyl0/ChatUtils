$ErrorActionPreference = "Stop"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  ChatUtils Derleme Baslatiliyor...      " -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan

# JDK 26.0.2 / En yuksek JDK tespiti
$javac = "C:\Program Files\Java\jdk-26.0.2.1\bin\javac.exe"
$jar = "C:\Program Files\Java\jdk-26.0.2.1\bin\jar.exe"

if (!(Test-Path $javac)) {
    $javac = "C:\Program Files\Java\latest\bin\javac.exe"
    $jar = "C:\Program Files\Java\latest\bin\jar.exe"
}
if (!(Test-Path $javac)) {
    $javac = "javac"
    $jar = "jar"
}

Write-Host "Kullanilan Derleyici: $javac" -ForegroundColor DarkCyan

if (!(Test-Path "lib")) {
    New-Item -ItemType Directory -Path "lib" | Out-Null
}

# Gerekli ek bagimliliklari indir (Adventure + BungeeChat + Annotations)
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$wc = New-Object Net.WebClient

$adVer = "5.2.0"
$adDeps = @(
    "adventure-api",
    "adventure-key",
    "adventure-text-serializer-plain",
    "adventure-text-serializer-gson",
    "adventure-text-serializer-legacy",
    "adventure-text-minimessage"
)

foreach ($dep in $adDeps) {
    $target = "lib\$dep.jar"
    $url = "https://repo1.maven.org/maven2/net/kyori/$dep/$adVer/$dep-$adVer.jar"
    try {
        $wc.DownloadFile($url, $target)
    } catch {
        try {
            $wc.DownloadFile("https://repo1.maven.org/maven2/net/kyori/$dep/4.19.0/$dep-4.19.0.jar", $target)
        } catch {}
    }
}

try {
    $wc.DownloadFile("https://repo1.maven.org/maven2/org/jetbrains/annotations/24.1.0/annotations-24.1.0.jar", "lib\annotations.jar")
    $wc.DownloadFile("https://repo1.maven.org/maven2/net/md-5/bungeecord-chat/1.20-R0.2/bungeecord-chat-1.20-R0.2.jar", "lib\bungeecord-chat.jar")
} catch {}

$buildDir = "target\classes"
if (Test-Path "target") {
    Remove-Item -Recurse -Force "target"
}
New-Item -ItemType Directory -Force -Path $buildDir | Out-Null

# Java kaynak dosyalarini topla
$sources = (Get-ChildItem -Recurse -Filter "*.java" -Path "src\main\java").FullName
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllLines("target\sources.txt", $sources, $utf8NoBom)

Write-Host "Kaynak kodlar derleniyor..." -ForegroundColor Green
$cpJars = (Get-ChildItem -Path "lib\*.jar").FullName
$classpath = $cpJars -join ";"

& $javac -encoding UTF-8 -cp $classpath -d $buildDir "@target\sources.txt"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Derleme hatasi!" -ForegroundColor Red
    exit 1
}

# Kaynak dosyalari kopyala (plugin.yml, config.yml, messages.yml)
Write-Host "Kaynak dosyalari paketleniyor..." -ForegroundColor Green
Copy-Item -Recurse -Force "src\main\resources\*" $buildDir

# JAR olustur
$targetJar = "ChatUtils-1.0.0.jar"
& $jar --create --file $targetJar -C $buildDir .
Copy-Item -Force $targetJar "target\ChatUtils-1.0.0.jar"
Copy-Item -Force $targetJar "ChatUtils.jar"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  TEBRIKLER! DERLEME BASARILI!           " -ForegroundColor Green
Write-Host "  Olusturulan JAR: $targetJar ve ChatUtils.jar" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan
