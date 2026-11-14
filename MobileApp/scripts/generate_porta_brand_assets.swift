#!/usr/bin/env swift
import CoreGraphics
import Foundation
import ImageIO

// One geometric mark supplies the launcher, splash, and desktop raster assets.
// The SVG and Android vector below mirror markPath() for resolution-independent use.
private let canvas: CGFloat = 1024
private let sRGB = CGColorSpace(name: CGColorSpace.sRGB)!
private let violet = color(0x5E, 0x4D, 0xB2)
private let lightViolet = color(0xB9, 0xAC, 0xEA)
private let paper = color(0xF7, 0xF5, 0xF2)

private func color(_ red: Int, _ green: Int, _ blue: Int) -> CGColor {
    CGColor(
        colorSpace: sRGB,
        components: [CGFloat(red) / 255, CGFloat(green) / 255, CGFloat(blue) / 255, 1]
    )!
}

private func markPath() -> CGPath {
    let path = CGMutablePath()
    path.move(to: CGPoint(x: 230, y: 850))
    path.addLine(to: CGPoint(x: 230, y: 445))
    path.addCurve(to: CGPoint(x: 512, y: 170), control1: CGPoint(x: 230, y: 291), control2: CGPoint(x: 356, y: 170))
    path.addCurve(to: CGPoint(x: 794, y: 445), control1: CGPoint(x: 668, y: 170), control2: CGPoint(x: 794, y: 291))
    path.addLine(to: CGPoint(x: 794, y: 850))
    path.addLine(to: CGPoint(x: 706, y: 850))
    path.addLine(to: CGPoint(x: 706, y: 445))
    path.addCurve(to: CGPoint(x: 512, y: 258), control1: CGPoint(x: 706, y: 337), control2: CGPoint(x: 619, y: 258))
    path.addCurve(to: CGPoint(x: 318, y: 445), control1: CGPoint(x: 405, y: 258), control2: CGPoint(x: 318, y: 337))
    path.addLine(to: CGPoint(x: 318, y: 850))
    path.closeSubpath()

    step(path, x: 330, top: 772, bottom: 850, shoulder: 402)
    step(path, x: 438, top: 660, bottom: 734, shoulder: 506)
    step(path, x: 548, top: 548, bottom: 622, shoulder: 612)
    return path
}

private func step(_ path: CGMutablePath, x: CGFloat, top: CGFloat, bottom: CGFloat, shoulder: CGFloat) {
    path.move(to: CGPoint(x: x, y: bottom))
    path.addLine(to: CGPoint(x: x, y: top + 44))
    path.addCurve(
        to: CGPoint(x: shoulder, y: top),
        control1: CGPoint(x: x, y: top + 18),
        control2: CGPoint(x: shoulder - 28, y: top),
    )
    path.addLine(to: CGPoint(x: 706, y: top))
    path.addLine(to: CGPoint(x: 706, y: bottom))
    path.closeSubpath()
}

private func render(size: Int, fill: CGColor, background: CGColor? = nil, scale: CGFloat = 1) -> Data {
    let bitmapInfo = (background == nil ? CGImageAlphaInfo.premultipliedLast : .noneSkipLast).rawValue
    guard let context = CGContext(
        data: nil,
        width: size,
        height: size,
        bitsPerComponent: 8,
        bytesPerRow: 0,
        space: sRGB,
        bitmapInfo: bitmapInfo
    ) else {
        fatalError("Could not allocate \(size)px image")
    }
    context.setAllowsAntialiasing(true)
    context.setShouldAntialias(true)
    if let background {
        context.setFillColor(background)
        context.fill(CGRect(x: 0, y: 0, width: size, height: size))
    }
    context.translateBy(x: CGFloat(size) / 2, y: CGFloat(size) / 2)
    context.scaleBy(x: CGFloat(size) / canvas * scale, y: -CGFloat(size) / canvas * scale)
    context.translateBy(x: -canvas / 2, y: -canvas / 2)
    context.addPath(markPath())
    context.setFillColor(fill)
    context.fillPath()
    guard let image = context.makeImage() else {
        fatalError("Could not create PNG image")
    }
    let output = NSMutableData()
    guard let destination = CGImageDestinationCreateWithData(output, "public.png" as CFString, 1, nil) else {
        fatalError("Could not encode PNG")
    }
    CGImageDestinationAddImage(destination, image, nil)
    guard CGImageDestinationFinalize(destination) else { fatalError("Could not finish PNG") }
    return output as Data
}

private func write(_ data: Data, to path: String) throws {
    let url = URL(fileURLWithPath: path)
    try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
    try data.write(to: url, options: .atomic)
}

let root = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let assets = root.appendingPathComponent("brand")
let android = root.appendingPathComponent("androidApp/src/main/res")
let ios = root.appendingPathComponent("iosApp/iosApp/Assets.xcassets")
let desktop = root.appendingPathComponent("desktopApp/src/main/resources")

let vectorPath = "M230,850 L230,445 C230,291 356,170 512,170 C668,170 794,291 794,445 L794,850 L706,850 L706,445 C706,337 619,258 512,258 C405,258 318,337 318,445 L318,850 Z M330,850 L330,816 C330,790 374,772 402,772 L706,772 L706,850 Z M438,734 L438,704 C438,678 478,660 506,660 L706,660 L706,734 Z M548,622 L548,592 C548,566 584,548 612,548 L706,548 L706,622 Z"
let svg = """
<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">
  <path fill="#5E4DB2" d="\(vectorPath)"/>
</svg>
"""
try write(Data(svg.utf8), to: assets.appendingPathComponent("porta-mark.svg").path)
func vectorXML(size: Int, fill: String) -> Data {
    Data("""
    <vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="\(size)dp"
        android:height="\(size)dp"
        android:viewportWidth="1024"
        android:viewportHeight="1024">
        <path
            android:fillColor="\(fill)"
            android:pathData="\(vectorPath)" />
    </vector>

    """.utf8)
}
try write(vectorXML(size: 180, fill: "#FF5E4DB2"), to: root.appendingPathComponent("designsystem/src/commonMain/composeResources/drawable/ic_logo.xml").path)
try write(vectorXML(size: 108, fill: "#FF000000"), to: android.appendingPathComponent("drawable/ic_launcher_monochrome.xml").path)
try write(render(size: 1024, fill: violet, background: paper, scale: 1.12), to: assets.appendingPathComponent("porta-app-icon-1024.png").path)
try write(render(size: 1024, fill: violet, scale: 1.18), to: assets.appendingPathComponent("porta-mark-1024.png").path)

for size in [20, 29, 40, 50, 57, 58, 60, 72, 76, 80, 87, 100, 114, 120, 144, 152, 167, 180, 1024] {
    try write(
        render(size: size, fill: violet, background: paper, scale: 1.12),
        to: ios.appendingPathComponent("AppIcon.appiconset/\(size).png").path
    )
}
for (suffix, size) in [("", 120), ("@2x", 240), ("@3x", 360)] {
    try write(render(size: size, fill: violet, scale: 1.18), to: ios.appendingPathComponent("ic_logo.imageset/ic_logo\(suffix).png").path)
    try write(render(size: size, fill: lightViolet, scale: 1.18), to: ios.appendingPathComponent("ic_logo.imageset/ic_logo_dark\(suffix).png").path)
}

for (density, multiplier) in [("mdpi", 1.0), ("hdpi", 1.5), ("xhdpi", 2.0), ("xxhdpi", 3.0), ("xxxhdpi", 4.0)] {
    let folder = android.appendingPathComponent("mipmap-\(density)")
    let foregroundSize = Int(108 * multiplier)
    let legacySize = Int(48 * multiplier)
    try write(render(size: foregroundSize, fill: violet), to: folder.appendingPathComponent("ic_launcher_foreground.png").path)
    let legacy = render(size: legacySize, fill: violet, background: paper, scale: 1.12)
    try write(legacy, to: folder.appendingPathComponent("ic_launcher.png").path)
    try write(legacy, to: folder.appendingPathComponent("ic_launcher_round.png").path)
}

try write(render(size: 512, fill: violet, background: paper, scale: 1.12), to: desktop.appendingPathComponent("porta-icon.png").path)
let iconset = desktop.appendingPathComponent("porta.iconset")
for (name, size) in [
    ("icon_16x16.png", 16), ("icon_16x16@2x.png", 32),
    ("icon_32x32.png", 32), ("icon_32x32@2x.png", 64),
    ("icon_128x128.png", 128), ("icon_128x128@2x.png", 256),
    ("icon_256x256.png", 256), ("icon_256x256@2x.png", 512),
    ("icon_512x512.png", 512), ("icon_512x512@2x.png", 1024),
] {
    try write(render(size: size, fill: violet, background: paper, scale: 1.12), to: iconset.appendingPathComponent(name).path)
}
// Modern ICNS entries may embed PNG data directly. This also works when
// iconutil cannot read an iconset generated in a sandboxed build environment.
var entries = Data()
for (type, name) in [
    ("icp4", "icon_16x16.png"), ("icp5", "icon_32x32.png"),
    ("ic07", "icon_128x128.png"), ("ic08", "icon_256x256.png"),
    ("ic09", "icon_512x512.png"), ("ic10", "icon_512x512@2x.png"),
    ("ic11", "icon_16x16@2x.png"), ("ic12", "icon_32x32@2x.png"),
    ("ic13", "icon_128x128@2x.png"), ("ic14", "icon_256x256@2x.png"),
] {
    let png = try Data(contentsOf: iconset.appendingPathComponent(name))
    entries.append(contentsOf: type.utf8)
    let length = UInt32(png.count + 8)
    for shift in stride(from: 24, through: 0, by: -8) { entries.append(UInt8((length >> shift) & 0xff)) }
    entries.append(png)
}
var icns = Data("icns".utf8)
let icnsLength = UInt32(entries.count + 8)
for shift in stride(from: 24, through: 0, by: -8) { icns.append(UInt8((icnsLength >> shift) & 0xff)) }
icns.append(entries)
try write(icns, to: desktop.appendingPathComponent("porta.icns").path)
try FileManager.default.removeItem(at: iconset)

// ICO stores a PNG payload; Windows Vista and later support this format.
let icoPNG = render(size: 256, fill: violet, background: paper, scale: 1.12)
var ico = Data([0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 32, 0])
for value in [UInt32(icoPNG.count), UInt32(22)] {
    for shift in stride(from: 0, to: 32, by: 8) { ico.append(UInt8((value >> shift) & 0xff)) }
}
ico.append(icoPNG)
try write(ico, to: desktop.appendingPathComponent("porta.ico").path)

print("Generated Porta launcher, splash, and desktop brand assets")
