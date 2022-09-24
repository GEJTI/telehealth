package com.example.telehealth.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.telehealth.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {

    public static void saveBitmapToFile(Bitmap bitmapImage, Context context, String title){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showTerms(title, bitmapImage, context);
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private static void showTerms(String title, Bitmap bitmapImage, Context context)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences("termsPref", Context.MODE_PRIVATE);
        boolean isAccepted = sharedPreferences.getBoolean("isAccepted" + title, false);
        if(isAccepted)
        {
            saveQRBitmap(bitmapImage, title, context);
            return;
        }
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        View mView = LayoutInflater.from(context).inflate(R.layout.dialog_terms,null);
        TextView txtTerms = mView.findViewById(R.id.txtTerms);
        txtTerms.setText(Html.fromHtml("<p><b>IMPORTANT</b> – DO NOT USE THIS SERVICE FOR EMERGENCY MEDICAL " +
                "NEEDS OR URGENT SITUATIONS. IMMEDIATELY GO " +
                "TO YOUR LOCAL HOSPITAL IF YOU BELIEVE YOU HAVE A MEDICAL EMERGENCY. " +
                "TELEMEDICINE SERVICES ARE NOT INTENDED TO REPLACE THE RELATIONSHIP" +
                " BETWEEN A DOCTOR AND A PATIENT. Rather than that, IT IS A SERVICE THAT PROVIDES CONVENIENCE AND ACCESS TO A WIDE VARIETY OF HEALTHCARE SERVICES. THE HEALTHCARE PROVIDER IS ONLY RESPONSIBLE FOR PROVIDING YOU WITH TELEMEDICINE CONSULTATIONS AND REMOTE PATIENT MONITORING SERVICES AS DETAILED IN THE PACKAGE TO WHICH YOU HAVE SUBSCRIBED. YOUR ELIGIBILITY TO USE THE HEALTHCARE PROVIDER'S TELEMEDICINE CONSULTATIONS AND REMOTE PATIENT MONITORING SERVICE IS SUBJECT TO THE DOCTOR'S APPROVAL " +
                "IN ACCORDANCE WITH THE TELEMEDICINE NATIONAL GUIDELINES 2015. </p>" +
                "<p>&emsp;&emsp;YOUR AGREEMENT TO ABIDE BY AND BE BOUND BY THESE TERMS AND CONDITIONS (INCLUDING OUR END USER LICENSE AGREEMENT AND TERMS OF SERVICE) AND PRIVACY POLICY IS DEEMED TO HAVE OCCURRED UPON YOUR FIRST USE OF OUR APPS OR WEBSITES AND BY EXPLICIT AGREEMENT UPON REGISTRATION.</p>" +
                "<p>&emsp;&emsp;YOUR PERSONAL INFORMATION WILL BE RETAINED IN ACCORDANCE WITH " +
                "THE MINISTRY OF HEALTH'S GUIDELINES FOR PATIENT DATA RETENTION. " +
                "Additionally, BY ACCESSING OUR CONSULTATION SERVICES, YOU ARE DEEMED " +
                "TO HAVE ACCEPTED OUR TERMS AND CONDITIONS, PRIVACY POLICY, AND ANY " +
                "MEDICAL CONSENT REQUIRED BY THE HEALTHCARE PROVIDER (COLLECTIVELY \"OUR POLICIES\"). BY EXPLICITLY ACCEPTING OUR POLICIES, YOU AUTHORIZE HEARTVOICE TO USE YOUR PERSONAL INFORMATION, INCLUDING YOUR HEALTH INFORMATION AND DETAILS OF ALL PREVIOUS CONSULTATIONS, COLLECTIVELY YOUR \"ELECTRONIC HEALTH RECORD,\" WHICH CONTAINS " +
                "\"PERSONAL DATA\" AND \"SENSITIVE PERSONAL DATA\".</p>" +
                "<p><b>ACCOUNTS</b></p>" +
                "<p>&emsp;&emsp;Certain features of Our Apps and Websites (such as the ability to buy Products and Services) may need you to establish (\"register\") an Account. " +
                "If you are under the legal age of majority to contract (i.e. you are under the age of 21), you may not create an Account unless your parent, guardian, or caregiver grants approval and/or registers their own \"Master\" Account and adds you as a Dependant. Dependents may only access the Master Account under the supervision of their parent, guardian, or caregiver and at the medical practitioner's discretion. </p>" +
                "<p>&emsp;&emsp;To be clear, Medicare accepts no responsibility for the medical practitioner's acts or behaviour during consultations with patients of any age. " +
                "When establishing an Account, you must give correct and full information. If any of your information changes in the future, you may update it in the patient information section of the mobile app. False information may result in the suspension of the Account.</p>" +
                "<p><b>INTELLECTUAL PROPERTY RIGHTS</b></p>" +
                "<p>&emsp;&emsp;Medicare and mobile app  incorporate, embody, and are based on globally patented or patentable inventions, trade secrets, copyrights, and other intellectual property rights (collectively \"Intellectual Property Rights\") owned or licensed by Medicare  and its licensors. " +
                "Medicare owns or licenses all logos and trademarks shown on our Apps and Websites. HeartVoice expressly retains all rights to their respective usage in the following paragraphs. " +
                "Except as expressly permitted in writing by Medicare, you may not duplicate, copy, distribute, sell, rent, sub-license, store, or in any other way re-use Content from Our mobile app. " +
                "Nothing in this Agreement transfers title or ownership of Medicare. </p>" +
                "<p>&emsp;&emsp;You agree not to delete, change, or conceal Medicare trademarks or property notices. " +
                "Any personal use of the Doctors License may be punishable. According to the Act Law of 1950 regarding the ACT PROVIDING FOR THE PROTECTION OF LAYOUT-DESIGNS (TOPOGRAPHIES) OF INTEGRATED CIRCUITS, AMENDING FOR THE PURPOSE CERTAIN SECTIONS OF REPUBLIC ACT NO. 8293, OTHERWISE KNOWN AS THE INTELLECTUAL PROPERTY CODE OF THE PHILIPPINES AND FOR OTHER PURPOSES. Any use of the said license may be punishable by " +
                "6 months - 1 year imprisonment for the obtruction of private property including Doctors liscence.</p>" +
                "<p><b>ACCEPTABLE USE POLICY</b></p>" +
                "<p>&emsp;&emsp;You may use Our Apps and Websites only in a legal and compliant way consistent with the conditions of this Clause 12.\n" +
                "You agree that Medicare  may limit, suspend, or terminate your access to any or all of its Services at any time and without notice if your use of the Medicare Services violates Medicare's current Acceptable Usage Policy, as established from time to time. " +
                " In particular, when submitting User Content (or communicating in any other way via Our Apps and Websites), you must not submit, communicate, or otherwise do anything that is sexually explicit; obscene, deliberately offensive, hateful, or otherwise inflammatory; promotes violence; promotes or assists in any form of unlawful activity; discriminates against, or is otherwise defamatory of, any person, group or class of persons, race, sex, religion, nationality, or disability. " +
                "</p><p>&emsp;&emsp;You must not infringe or assist in the infringement of another party's intellectual property rights (including, but not limited to, copyright, patents, trademarks, designs, and database rights); or violate any legal obligation owed to a third party, including, but not limited to, contractual obligations and duties of confidence. " +
                "Medicare retains the right to immediately suspend or terminate your access to Our Apps and Websites if, in Our sole discretion, you seriously violate the requirements of this Clause or any other aspect of these Terms and Conditions. " +
                "Medicare expressly disclaims all responsibility for any actions (including, but not limited to, those listed above) taken in response to a violation of these Terms and Conditions.</p>"));

        mBuilder.setView(mView);

        final AlertDialog dialog = mBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        Button btnAccept = (Button) mView.findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveQRBitmap(bitmapImage, title, context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isAccepted" + title, true);
                editor.apply();
                dialog.dismiss();
            }
        });

        int width = (int)(context.getResources().getDisplayMetrics().widthPixels*0.95);
        int height = (int)(context.getResources().getDisplayMetrics().heightPixels*0.95);
        dialog.getWindow().setLayout(width, height);
    }

    private static void saveQRBitmap(Bitmap bitmapImage, String title, Context context)
    {
        ContentResolver cr = context.getContentResolver();
        String description = "QR Code";
        String savedURL = MediaStore.Images.Media
                .insertImage(cr, bitmapImage, title, description);

        String msg = savedURL.isEmpty()? "Failed to saved" : "QR Code saved";

        Toast.makeText(context, msg,
                Toast.LENGTH_LONG).show();
    }

}
