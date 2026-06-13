package io.senovative.panorama.sdk

enum class PanoramaFailure(val message: String) {
    OpenCvUnavailable("OpenCV belum bisa dimuat di device ini."),
    NeedMoreFrames("Ambil minimal dua foto untuk membuat panorama."),
    NotEnoughFeatures("Foto kurang punya detail untuk dicocokkan."),
    NotEnoughMatches("Overlap antar foto belum cukup."),
    DirectionMismatch("Arah gerakan belum sesuai mode panorama."),
    HomographyFailed("OpenCV gagal menghitung transform antar foto."),
    RenderFailed("Panorama gagal dibuat.")
}
