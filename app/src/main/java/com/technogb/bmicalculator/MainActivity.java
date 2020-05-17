package com.technogb.bmicalculator;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String KEY_VALUE_ZERO = "0";
    private static final String KEY_VALUE_DOT = ".";

    private static final int INDEX_CENTIMETER = 0;
    private static final int INDEX_POUND = 1;
    private static final int INDEX_FEET = 2;
    private static final int INDEX_INCH = 3;

    private static final double POUND_KG_CONVERSION_RATE = 0.453592;
    private static final double FEET_METER_CONVERSION_RATE = 0.3048;
    private static final double CENTIMETER_METER_CONVERSION_RATE = 0.01;
    private static final double INCH_METER_CONVERSION_RATE = 0.0254;

    private static final String KEY_WEIGHT_VALUE = "weight_value";
    private static final String KEY_WEIGHT_LABEL = "weight_label";
    private static final String KEY_HEIGHT_VALUE = "height_value";
    private static final String KEY_HEIGHT_LABEL = "height_label";

    private static final String KEY_KEYBOARD_VISIBILITY = "keyboard_visible";
    private static final String KEY_RESULT_VISIBILITY = "result_visible";
    private static final String KEY_BMI_RESULT = "bmi_result";
    private static final String KEY_PERSON_WEIGHT_STATUS = "weight_category";

    private static final double BMI_UPPER_BOUND = 60.0;
    private static final double BMI_LOWER_BOUND = 0.0;
    private static final double UNDERWEIGHT_UPPER_BOUND = 18.5;
    private static final int NORMAL_UPPER_BOUND = 25;
    private final String[] weightOptions = {"Kilogram", "Pound"};
    private final String[] heightOption = {"Centimeter", "Meter", "Feet", "Inch"};
    private TextView mWeight, mHeight, mBmiResultValue, mWeightCategory,
            mWeightLabel, mHeightLabel;
    private View mBmiResult, mKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customActionBar();

        mWeight = findViewById(R.id.tv_weight);
        mHeight = findViewById(R.id.tv_height);
        mBmiResultValue = findViewById(R.id.tv_bmi_result_value);
        mWeightLabel = findViewById(R.id.tv_weight_unit_label);
        mHeightLabel = findViewById(R.id.tv_height_unit_label);
        mWeightCategory = findViewById(R.id.tv_person_weight_status);
        mKeyboard = findViewById(R.id.bmi_keyboard);
        mBmiResult = findViewById(R.id.show_bmi_result);

        findViewById(R.id.button_zero).setOnClickListener(this);
        findViewById(R.id.button_one).setOnClickListener(this);
        findViewById(R.id.button_two).setOnClickListener(this);
        findViewById(R.id.button_three).setOnClickListener(this);
        findViewById(R.id.button_four).setOnClickListener(this);
        findViewById(R.id.button_five).setOnClickListener(this);
        findViewById(R.id.button_six).setOnClickListener(this);
        findViewById(R.id.button_seven).setOnClickListener(this);
        findViewById(R.id.button_eight).setOnClickListener(this);
        findViewById(R.id.button_nine).setOnClickListener(this);
        findViewById(R.id.button_dot).setOnClickListener(this);
        findViewById(R.id.button_back).setOnClickListener(this);
        findViewById(R.id.button_ac).setOnClickListener(this);
        findViewById(R.id.button_go).setOnClickListener(this);
        findViewById(R.id.layout_weight_unit).setOnClickListener(this);
        findViewById(R.id.layout_height_unit).setOnClickListener(this);

        mWeight.setTextColor(this.getResources().getColor(R.color.bmiTextOrangeColor));
        mHeight.setTextColor(this.getResources().getColor(R.color.bmiTextDefaultColor));

        mWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWeight.setSelected(true);
                mHeight.setSelected(false);
                changeStateAndEditUnit();

            }
        });

        mHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHeight.setSelected(true);
                mWeight.setSelected(false);
                changeStateAndEditUnit();
            }
        });

        restoreActivityState(savedInstanceState);
    }

    private void restoreActivityState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String saveBmiResult = savedInstanceState.getString(KEY_BMI_RESULT);

            mWeight.setText(savedInstanceState.getString(KEY_WEIGHT_VALUE));
            mHeight.setText(savedInstanceState.getString(KEY_HEIGHT_VALUE));
            mWeightLabel.setText(savedInstanceState.getString(KEY_WEIGHT_LABEL));
            mHeightLabel.setText(savedInstanceState.getString(KEY_HEIGHT_LABEL));
            mBmiResultValue.setText(savedInstanceState.getString(KEY_BMI_RESULT));
            mWeightCategory.setText(savedInstanceState.getString(KEY_PERSON_WEIGHT_STATUS));

            int saveResultVisibility = savedInstanceState.getInt(KEY_RESULT_VISIBILITY);

            if (saveResultVisibility == View.VISIBLE) {
                showResult();
            } else {
                showKeyboard();
            }

            if (saveBmiResult != null) {
                setBmiStatus(Double.parseDouble(saveBmiResult));
            }
        } else {
            mBmiResultValue.setText(getResources().getText(R.string.result_default_text));
        }
    }

    private void changeStateAndEditUnit() {
        if (mWeight.isSelected()) {
            mWeight.setTextColor(this.getResources().getColor(R.color.bmiTextOrangeColor));
            mHeight.setTextColor(this.getResources().getColor(R.color.bmiTextDefaultColor));
            showKeyboard();
        }

        if (mHeight.isSelected()) {
            mHeight.setTextColor(this.getResources().getColor(R.color.bmiTextOrangeColor));
            mWeight.setTextColor(this.getResources().getColor(R.color.bmiTextDefaultColor));
            showKeyboard();
        }
    }

    private void calculateBmi() {
        String weight = mWeight.getText().toString().trim();
        String height = mHeight.getText().toString();

        if (weight.equals(KEY_VALUE_DOT) || height.equals(KEY_VALUE_DOT)) {
            Toast.makeText(this, getResources().getText(R.string.invalid_message)
                    , Toast.LENGTH_SHORT).show();
        } else {
            double weightValue = getWeightInKg(weight);
            double heightValue = getHeightInMeter(height);

            if (heightValue == BMI_LOWER_BOUND ||
                    (heightValue == BMI_LOWER_BOUND && weightValue == BMI_LOWER_BOUND)) {
                Toast.makeText(this, getResources().getText(R.string.invalid_message)
                        , Toast.LENGTH_SHORT).show();
            } else {
                double bmi = weightValue / Math.pow(heightValue, 2);

                if (bmi > BMI_UPPER_BOUND) {
                    Toast.makeText(this, getResources().getText(R.string.invalid_message)
                            , Toast.LENGTH_SHORT).show();
                } else {
                    DecimalFormat decimalFormat = new DecimalFormat("#.#");
                    double bmiResultInFormat = Double.parseDouble(decimalFormat.format(bmi));
                    mBmiResultValue.setText(String.valueOf(bmiResultInFormat));
                    setBmiStatus(bmi);
                    showResult();
                }
            }
        }
    }

    private void setBmiStatus(double bmi) {
        if (bmi < UNDERWEIGHT_UPPER_BOUND) {
            mWeightCategory.setTextColor(this.getResources().getColor(R.color.underWeightColor));
            mWeightCategory.setText(this.getResources().getString(R.string.underWeight));
        } else if (bmi >= UNDERWEIGHT_UPPER_BOUND && bmi < NORMAL_UPPER_BOUND) {
            mWeightCategory.setTextColor(this.getResources().getColor(R.color.normalWeightColor));
            mWeightCategory.setText(this.getResources().getString(R.string.normal));
        } else if (bmi > NORMAL_UPPER_BOUND) {
            mWeightCategory.setTextColor(this.getResources().getColor(R.color.overWeightColor));
            mWeightCategory.setText(this.getResources().getString(R.string.overWeight));
        }
    }

    private double getHeightInMeter(String height) {
        double heightValue = Double.parseDouble(height);
        String heightLabel = mHeightLabel.getText().toString();
        if (heightLabel.equals(heightOption[INDEX_CENTIMETER])) {
            heightValue *= CENTIMETER_METER_CONVERSION_RATE;
        } else if (heightLabel.equals(heightOption[INDEX_FEET])) {
            heightValue *= FEET_METER_CONVERSION_RATE;
        } else if (heightLabel.equals(heightOption[INDEX_INCH])) {
            heightValue *= INCH_METER_CONVERSION_RATE;
        }
        return heightValue;
    }

    private double getWeightInKg(String weight) {
        String weightLabel = mWeightLabel.getText().toString();
        double weightValue = Double.parseDouble(weight);
        if (weightLabel.equals(weightOptions[INDEX_POUND])) {
            weightValue *= POUND_KG_CONVERSION_RATE;
        }
        return weightValue;
    }

    private void showResult() {
        mKeyboard.setVisibility(View.GONE);
        mBmiResult.setVisibility(View.VISIBLE);
    }

    private void showKeyboard() {
        mKeyboard.setVisibility(View.VISIBLE);
        mBmiResult.setVisibility(View.GONE);
    }

    private void setHeight(String heightValue) {
        String textHeight = mHeight.getText().toString();
        int textLength = textHeight.length();
        if (textHeight.equals(KEY_VALUE_ZERO)) {
            textHeight = "";
        }
        if (textLength == 3 && !hasDot(textHeight) && heightValue.equals(".")) {
            String updateTextHeight = textHeight + heightValue;
            mHeight.setText(updateTextHeight);
        }
        if (!hasDot(textHeight)) {

            if (textLength < 3 && mHeight.isSelected()) {
                String updateTextHeight = textHeight + heightValue;
                mHeight.setText(updateTextHeight);
            }
        } else {
            if (containsOneDot(textHeight) && !heightValue.equals(KEY_VALUE_DOT)) {
                int lengthAfterDot = getLengthAfterDot(textHeight);
                if (lengthAfterDot < 3 && mHeight.isSelected()) {
                    String updateTextHeight = textHeight + heightValue;
                    mHeight.setText(updateTextHeight);
                }
            }
        }

    }

    private void setWeight(String weight) {
        String textWeight = mWeight.getText().toString();
        int textLength = textWeight.length();
        if (textWeight.equals(KEY_VALUE_ZERO)) {
            textWeight = "";
        }
        if (textLength == 3 && !hasDot(textWeight) && weight.equals(KEY_VALUE_DOT)) {
            String updateTextHeight = textWeight + weight;
            mWeight.setText(updateTextHeight);
        }
        if (!hasDot(textWeight)) {

            if (textLength < 3 && mWeight.isSelected()) {
                String updateTextHeight = textWeight + weight;
                mWeight.setText(updateTextHeight);
            }
        } else {
            if (containsOneDot(textWeight) && !weight.equals(KEY_VALUE_DOT)) {
                int lengthAfterDot = getLengthAfterDot(textWeight);
                if (lengthAfterDot < 3 && mWeight.isSelected()) {
                    String updateTextHeight = textWeight + weight;
                    mWeight.setText(updateTextHeight);
                }
            }
        }
    }

    private boolean containsOneDot(String text) {
        int counter = 0;
        for (int digit = 0; digit < text.length(); ++digit) {
            if (text.charAt(digit) == '.') {
                counter += 1;
            }

            if (counter > 1) {
                return false;
            }
        }
        return true;
    }

    private int getLengthAfterDot(String text) {
        int lengthCounter, position = 0;
        for (int oneDigit = 0; oneDigit < text.length(); ++oneDigit) {
            if (text.charAt(oneDigit) == '.') {
                position = oneDigit;
                break;
            }
        }
        lengthCounter = text.substring(position).length();
        return lengthCounter;
    }

    private boolean hasDot(String text) {
        for (int oneDigit = 0; oneDigit < text.length(); ++oneDigit) {
            if (text.charAt(oneDigit) == '.') {
                return true;
            }
        }
        return false;
    }

    private void removeHeightLastDigit() {
        String textHeight = mHeight.getText().toString();
        if (textHeight.length() > 0) {
            textHeight = textHeight.substring(0, textHeight.length() - 1);
            if (textHeight.isEmpty()) {
                mHeight.setText(KEY_VALUE_ZERO);
            } else
                mHeight.setText(textHeight);
        }
    }

    private void removeWeightLastDigit() {
        String textHeight = mWeight.getText().toString();
        if (textHeight.length() > 0) {
            textHeight = textHeight.substring(0, textHeight.length() - 1);
            if (textHeight.isEmpty()) {
                mWeight.setText(KEY_VALUE_ZERO);
            } else
                mWeight.setText(textHeight);
        }
    }

    private void customActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);
        getSupportActionBar().getCustomView();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_WEIGHT_VALUE, mWeight.getText().toString());
        outState.putString(KEY_HEIGHT_VALUE, mHeight.getText().toString());
        outState.putString(KEY_WEIGHT_LABEL, mWeightLabel.getText().toString());
        outState.putString(KEY_HEIGHT_LABEL, mHeightLabel.getText().toString());
        outState.putInt(KEY_KEYBOARD_VISIBILITY, mKeyboard.getVisibility());
        outState.putInt(KEY_RESULT_VISIBILITY, mBmiResult.getVisibility());
        outState.putString(KEY_BMI_RESULT, mBmiResultValue.getText().toString());
        outState.putString(KEY_PERSON_WEIGHT_STATUS, mWeightCategory.getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_zero:
            case R.id.button_one:
            case R.id.button_two:
            case R.id.button_three:
            case R.id.button_four:
            case R.id.button_five:
            case R.id.button_six:
            case R.id.button_seven:
            case R.id.button_eight:
            case R.id.button_nine: {
                if (mHeight.isSelected()) {
                    setHeight(((Button) v).getText().toString());
                } else {
                    setWeight(((Button) v).getText().toString());
                }
                break;
            }
            case R.id.button_back: {
                if (mHeight.isSelected()) {
                    removeHeightLastDigit();
                } else {
                    removeWeightLastDigit();
                }
                break;
            }
            case R.id.button_ac: {
                if (mHeight.isSelected()) {
                    mHeight.setText(KEY_VALUE_ZERO);
                } else {
                    mWeight.setText(KEY_VALUE_ZERO);
                }
                break;
            }
            case R.id.button_dot: {
                if (mWeight.isSelected()) {
                    setWeight(((Button) v).getText().toString());
                } else {
                    setHeight(((Button) v).getText().toString());
                }
                break;
            }
            case R.id.button_go: {
                calculateBmi();
                break;
            }
            case R.id.layout_weight_unit: {
                AlertDialog.Builder weightOption = new AlertDialog.Builder(this);
                weightOption.setItems(weightOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWeightLabel.setText(weightOptions[which]);
                        mWeight.setTextColor(getResources().getColor(R.color.bmiTextOrangeColor));
                        mHeight.setTextColor(getResources().getColor(R.color.bmiTextDefaultColor));
                        mWeight.setSelected(true);
                        mHeight.setSelected(false);
                        showKeyboard();
                    }
                });
                weightOption.show();
                break;
            }
            case R.id.layout_height_unit: {
                AlertDialog.Builder heightOption = new AlertDialog.Builder(this);
                heightOption.setItems(this.heightOption, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHeightLabel.setText(MainActivity.this.heightOption[which]);
                        mHeight.setTextColor(getResources().getColor(R.color.bmiTextOrangeColor));
                        mWeight.setTextColor(getResources().getColor(R.color.bmiTextDefaultColor));
                        mWeight.setSelected(false);
                        mHeight.setSelected(true);
                        showKeyboard();
                    }
                });
                heightOption.show();
                break;
            }
        }
    }
}
