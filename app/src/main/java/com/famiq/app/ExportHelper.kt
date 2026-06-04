package com.famiq.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.famiq.app.data.model.Transaksi
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportHelper {

    fun formatRupiah(nominal: Long): String {
        val format = java.text.NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(nominal)
    }

    // ── EKSPOR KE CSV (EXCEL) ──
    fun exportToCSV(context: Context, data: List<Transaksi>, bulan: String, namaKeluarga: String) {
        val namaFileBersih = namaKeluarga.replace(" ", "_").replace("/", "")
        val fileName = "Laporan_${namaFileBersih}_${bulan.replace(" ", "_")}_${System.currentTimeMillis()}.csv"

        try {
            val csvContent = StringBuilder()
            // Judul dinamis di Excel
            csvContent.append("LAPORAN PENGELUARAN ${namaKeluarga.uppercase()}\n")
            csvContent.append("Periode/Bulan: $bulan\n\n")
            csvContent.append("Tanggal,Jam,Kategori,Catatan,Nominal,Diinput Oleh\n")

            var total = 0L
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            data.forEach { trx ->
                val date = Date(trx.tanggal)
                val tgl = dateFormat.format(date)
                val jam = timeFormat.format(date)
                val cat = trx.catatan.replace(",", " ")

                csvContent.append("$tgl,$jam,${trx.kategori.name},$cat,${trx.nominal},${trx.diinputOleh}\n")
                total += trx.nominal
            }

            csvContent.append(",,,,,\n")
            csvContent.append("TOTAL PENGELUARAN,,,,${total},\n")
            csvContent.append("\nLaporan Keuangan ini dibuat dengan aplikasi Famiq.\n")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Famiq")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        os.write(csvContent.toString().toByteArray())
                    }
                }
            } else {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dir = File(path, "Famiq")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { os ->
                    os.write(csvContent.toString().toByteArray())
                }
            }

            Toast.makeText(context, "Excel Berhasil! Cek folder Downloads/Famiq", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Gagal export CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ── EKSPOR KE PDF ──
    fun exportToPDF(context: Context, data: List<Transaksi>, bulan: String, namaKeluarga: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Ukuran A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // 🎨 1. GAMBAR WATERMARK FAMIQ DULU (Biar ada di belakang teks)
        val watermarkPaint = Paint().apply {
            textSize = 140f // Ukuran gede
            isFakeBoldText = true
            color = Color.rgb(20, 90, 50) // Hijau Famiq
            alpha = 25 // Opacity dikecilin banget biar samar (0-255)
            textAlign = Paint.Align.CENTER
        }
        canvas.save()
        // Puter canvasnya dari tengah halaman A4 (595/2, 842/2)
        canvas.translate(595f / 2, 842f / 2)
        canvas.rotate(-45f) // Miring diagonal dari bawah ke atas
        canvas.drawText("FAMIQ", 0f, 0f, watermarkPaint)
        canvas.restore() // Balikin posisi canvas buat nulis teks normal

        // 🎨 2. PERSIAPAN KUAS TEKS NORMAL
        val titlePaint = Paint().apply {
            textSize = 18f
            isFakeBoldText = true
            color = Color.rgb(20, 90, 50)
        }
        val textPaint = Paint().apply {
            textSize = 12f
            color = Color.BLACK
        }
        val boldPaint = Paint().apply {
            textSize = 12f
            isFakeBoldText = true
            color = Color.BLACK
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val footerPaint = Paint().apply {
            textSize = 10f
            color = Color.GRAY
            isFakeBoldText = true
        }

        // Header Laporan Dinamis
        canvas.drawText("LAPORAN PENGELUARAN ${namaKeluarga.uppercase()}", 50f, 50f, titlePaint)
        canvas.drawText("Periode/Bulan: $bulan", 50f, 75f, textPaint)
        canvas.drawText("Diekstrak pada: ${SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())}", 50f, 95f, textPaint)

        // Tabel Header
        var yPos = 130f
        canvas.drawLine(50f, yPos - 15f, 545f, yPos - 15f, linePaint)

        canvas.drawText("Tanggal", 50f, yPos, boldPaint)
        canvas.drawText("Kategori", 130f, yPos, boldPaint)
        canvas.drawText("Catatan", 230f, yPos, boldPaint)
        canvas.drawText("Nominal", 380f, yPos, boldPaint)
        canvas.drawText("Oleh", 480f, yPos, boldPaint)

        yPos += 10f
        canvas.drawLine(50f, yPos, 545f, yPos, linePaint)

        var total = 0L
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        // Isi Data
        yPos += 20f
        data.forEach { trx ->
            if (yPos > 800f) return@forEach

            canvas.drawText(dateFormat.format(Date(trx.tanggal)), 50f, yPos, textPaint)
            canvas.drawText(trx.kategori.name.take(10), 130f, yPos, textPaint)

            val shortNote = if(trx.catatan.length > 20) trx.catatan.take(17) + "..." else trx.catatan
            canvas.drawText(shortNote, 230f, yPos, textPaint)

            canvas.drawText(formatRupiah(trx.nominal), 380f, yPos, textPaint)
            canvas.drawText(trx.diinputOleh.take(12), 480f, yPos, textPaint)

            total += trx.nominal
            yPos += 20f
            canvas.drawLine(50f, yPos - 15f, 545f, yPos - 15f, linePaint)
        }

        // Total
        yPos += 15f
        canvas.drawText("TOTAL PENGELUARAN:", 230f, yPos, boldPaint)
        canvas.drawText(formatRupiah(total), 380f, yPos, boldPaint)

        // Footer Famiq
        yPos += 40f
        canvas.drawText("Laporan Keuangan ini dibuat dengan aplikasi Famiq.", 50f, yPos, footerPaint)

        pdfDocument.finishPage(page)

        val namaFileBersih = namaKeluarga.replace(" ", "_").replace("/", "")
        val fileName = "Laporan_${namaFileBersih}_${bulan.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Famiq")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        pdfDocument.writeTo(os)
                    }
                }
            } else {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dir = File(path, "Famiq")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)
                FileOutputStream(file).use { os ->
                    pdfDocument.writeTo(os)
                }
            }
            Toast.makeText(context, "PDF Berhasil! Cek folder Downloads/Famiq", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}