#include "fskdemodulation++.h"
#include "filt.h"

#include <stdio.h>
#include <stdlib.h>

struct FSKDemodulationState
{
    //TODO: Don't need class Filter
    Filter *bpf0;
    Filter *bpf1;
    float *outSquared0;
    float *outSquared1;
    int outSquaredId;

    float *outEnj0;
    float *outEnj1;
    int outEnjId;

    float previousSampleValue;

    int continuousPeakNumber;
    int previousPeakId;
    int first0PeakId;
    int lastPeakId;
    int sampleId;

    float lenb;
    float halfb;
    FSKDemodulationStatus status;

    short *binary;
    int binaryLength;
};

class FSKDemodulationPrivate {
public:
    FSKDemodulationPrivate(FSKDemodulationConfig &fskDemoConfig);
    ~FSKDemodulationPrivate();
    FSKDemodulationStatus processOne(float sample);
    int reset();
    char *output(int &textLength);
private:
    char *bits2Bytes(short *binary, int length, int &textLength);
    int isMaxima(float *data, int length, int id, float threshold);
    FSKDemodulationState fskDemoState;
    FSKDemodulationConfig &fskDemoConfig;
};

// TODO: Optimize?
int FSKDemodulationPrivate::isMaxima(float *data, int length, int id, float threshold)
{
    if (data[id] < threshold) {
        return false;
    }
    int rt = true;
    for (int i = 0; i < length; i++) {
        if (data[i] > data[id]) {
            rt = false;
            break;
        }
    }
    return rt;
}

char *FSKDemodulationPrivate::bits2Bytes(short *binary, int length, int &textLength)
{
    if (length == 0) {
        textLength = 0;
        return 0;
    }
    textLength = length / 8;
    char *text = new char[textLength];
    int textId;
    for (textId = 0; textId < textLength; textId++) {
        int x;
        for (x = 0; x < 8; x++) {
            if (binary[textId * 8 + x]) {
                text[textId] |= 1 << x;
            }
            else {
                text[textId] &= ~(1 << x);
            }
        }
    }
    return text;
}

char *FSKDemodulationPrivate::output(int &textLength)
{
    return bits2Bytes(fskDemoState.binary, fskDemoState.binaryLength, textLength);
}

char *FSKDemodulation::output(int &textLength)
{
    return d->output(textLength);
}

FSKDemodulationPrivate::FSKDemodulationPrivate(FSKDemodulationConfig &fskDemoConfig):
fskDemoConfig(fskDemoConfig)
{
    fskDemoState.bpf0 = new Filter(BPF, fskDemoConfig.numTaps, fskDemoConfig.samplingRate /
    1000.0, (fskDemoConfig.f0 - fskDemoConfig.bandWidth / 2) / 1000.0, (fskDemoConfig.f0 +
    fskDemoConfig.bandWidth / 2) / 1000.0);
    fskDemoState.bpf1 = new Filter(BPF, fskDemoConfig.numTaps, fskDemoConfig.samplingRate /
    1000.0, (fskDemoConfig.f1 - fskDemoConfig.bandWidth / 2) / 1000.0, (fskDemoConfig.f1 +
    fskDemoConfig.bandWidth / 2) / 1000.0);

    fskDemoState.sampleId = -1;

    fskDemoState.outSquared0 = new float[fskDemoConfig.enjNSum];
    fskDemoState.outSquared1 = new float[fskDemoConfig.enjNSum];
    fskDemoState.outSquaredId = -1;

    fskDemoState.outEnj0 = new float[fskDemoConfig.maximumInterval];
    fskDemoState.outEnj1 = new float[fskDemoConfig.maximumInterval];
    fskDemoState.outEnjId = -1;

    fskDemoState.continuousPeakNumber = 0;
    fskDemoState.previousPeakId = -1;
    fskDemoState.first0PeakId = -1;
    fskDemoState.lastPeakId = -1;
    fskDemoState.previousSampleValue = 0;


    fskDemoState.lenb = fskDemoConfig.samplingRate * (fskDemoConfig.activeTime +
    fskDemoConfig.silenceTime) / 1000.0;
    fskDemoState.halfb = fskDemoState.lenb / 2;
    fskDemoState.status = fskDemodulationStatusStart;


    fskDemoState.binary = new short[fskDemoConfig.maxBitNumber];
    fskDemoState.binaryLength = 0;

}

int FSKDemodulationPrivate::reset()
{
    fskDemoState.sampleId = -1;
    int i = 0;
    for (i = 0; i < fskDemoConfig.enjNSum; i++) {
        fskDemoState.outSquared0[i] = 0;
        fskDemoState.outSquared1[i] = 0;
    }
    fskDemoState.outSquaredId = -1;
    for (i = 0; i < fskDemoConfig.maximumInterval; i++) {
        fskDemoState.outEnj0[i] = 0;
        fskDemoState.outEnj1[i] = 0;
    }

    fskDemoState.outEnjId = -1;


    fskDemoState.continuousPeakNumber = 0;
    fskDemoState.previousPeakId = -1;
    fskDemoState.first0PeakId = -1;
    fskDemoState.lastPeakId = -1;
    fskDemoState.previousSampleValue = 0;

    fskDemoState.lenb = fskDemoConfig.samplingRate * (fskDemoConfig.activeTime +
                        fskDemoConfig.silenceTime) / 1000.0;
    fskDemoState.halfb = fskDemoState.lenb / 2;
    fskDemoState.status = fskDemodulationStatusStart;

    fskDemoState.binaryLength = 0;
    return 1;
}

int FSKDemodulation::reset()
{
    return d->reset();
}

FSKDemodulation::FSKDemodulation(FSKDemodulationConfig &fskDemoConfig)
{
    d = new FSKDemodulationPrivate(fskDemoConfig);
}

FSKDemodulationPrivate::~FSKDemodulationPrivate()
{
    delete fskDemoState.bpf0;
    delete fskDemoState.bpf1;

    delete [] fskDemoState.outSquared0;
    delete [] fskDemoState.outSquared1;

    delete [] fskDemoState.outEnj0;
    delete [] fskDemoState.outEnj1;
    delete [] fskDemoState.binary;
}


FSKDemodulation::~FSKDemodulation()
{
    delete d;
}


FSKDemodulationStatus FSKDemodulationPrivate::processOne(float sample)
{
    if (fskDemoState.status == fskDemodulationStatusEnd) {
        return fskDemoState.status;
    }
    // Differentiator
    fskDemoState.sampleId++;
    fskDemoState.outSquaredId++;
    if (fskDemoState.outSquaredId == fskDemoConfig.enjNSum) {
        fskDemoState.outSquaredId = 0;
    }
    fskDemoState.outEnjId++;
    if (fskDemoState.outEnjId == fskDemoConfig.maximumInterval) {
        fskDemoState.outEnjId = 0;
    }


    float input = sample - fskDemoState.previousSampleValue;
    fskDemoState.previousSampleValue = sample;


    float outBpf0;
    float outBpf1;

    // Filter
    outBpf0 = fskDemoState.bpf0->do_sample(input);
    outBpf1 = fskDemoState.bpf1->do_sample(input);

    fskDemoState.outSquared0[fskDemoState.outSquaredId] = outBpf0 * outBpf0;
    fskDemoState.outSquared1[fskDemoState.outSquaredId] = outBpf1 * outBpf1;


    fskDemoState.outEnj0[fskDemoState.outEnjId] = 0;
    fskDemoState.outEnj1[fskDemoState.outEnjId] = 0;

    int j;
    for (j = 0; j < fskDemoConfig.enjNSum; j++) {
        fskDemoState.outEnj0[fskDemoState.outEnjId] += fskDemoState.outSquared0[j];
        fskDemoState.outEnj1[fskDemoState.outEnjId] += fskDemoState.outSquared1[j];
    }
    fskDemoState.outEnj0[fskDemoState.outEnjId] /= j;
    fskDemoState.outEnj1[fskDemoState.outEnjId] /= j;


    if (fskDemoState.status == fskDemodulationStatusStart) {
        if (fskDemoState.sampleId == fskDemoConfig.maximumInterval / 2) {
            fskDemoState.status = fskDemodulationStatusFind1Peaks;
        }
        return fskDemoState.status;
    }

    if (fskDemoState.status == fskDemodulationStatusFindFirst0Peak) {
        if (fskDemoState.previousPeakId != -1) {
            if (fskDemoState.sampleId - fskDemoState.previousPeakId < fskDemoState.halfb) {
                return fskDemoState.status;
            }
        }
        int testId = (fskDemoState.outEnjId + fskDemoConfig.maximumInterval / 2) %
        fskDemoConfig.maximumInterval;
        int isMax = isMaxima(fskDemoState.outEnj0, fskDemoConfig.maximumInterval, testId,
        fskDemoConfig.thresholdEnj0);
        if (isMax && (fskDemoState.outEnj0[testId] / fskDemoState.outEnj1[testId] >= fskDemoConfig.binaryThreshold)) {
// printf("found marker enj0: %f\n", (float) (fskDemoState.sampleId) / fskDemoConfig.samplingRate);
            fskDemoState.first0PeakId = fskDemoState.sampleId - fskDemoConfig.maximumInterval / 2;
            fskDemoState.lastPeakId = fskDemoState.first0PeakId;
            fskDemoState.status = fskDemodulationStatusFirst0PeakFound;
        }
        return fskDemoState.status;
    }

    if (fskDemoState.status == fskDemodulationStatusFind1Peaks || fskDemoState.status ==
    fskDemodulationStatusFindFirst0Peak) {
        if (fskDemoState.previousPeakId != -1) {
            if (fskDemoState.sampleId - fskDemoState.previousPeakId < fskDemoState.halfb) {
                return fskDemoState.status;
            }
        }
        int testId = (fskDemoState.outEnjId + fskDemoConfig.maximumInterval / 2) %
        fskDemoConfig.maximumInterval;
        int isMax = isMaxima(fskDemoState.outEnj1, fskDemoConfig.maximumInterval, testId,
        fskDemoConfig.thresholdEnj1);
        if (!isMax) {
            return fskDemoState.status;
        }
// printf("found 1-peak: %f pre: %f, count: %d\n", (float) (fskDemoState.sampleId) /
//fskDemoConfig.samplingRate, (float) (fskDemoState.previousPeakId) / fskDemoConfig.samplingRate,
//fskDemoState.continuousPeakNumber);
        if (fskDemoState.previousPeakId != -1) {
            // Too far between two peaks, down to 1
            if (fskDemoState.sampleId - fskDemoConfig.maximumInterval / 2 -
            fskDemoState.previousPeakId < fskDemoState.halfb + fskDemoState.lenb) {
                fskDemoState.continuousPeakNumber++;
            }
            else {
                fskDemoState.continuousPeakNumber = 1;
            }
            fskDemoState.previousPeakId = fskDemoState.sampleId - fskDemoConfig.maximumInterval
            / 2;
        }
        else {
            fskDemoState.continuousPeakNumber = 1;
            fskDemoState.previousPeakId = fskDemoState.sampleId - fskDemoConfig.maximumInterval
            / 2;
        }
        if (fskDemoState.continuousPeakNumber >= fskDemoConfig.header1PeakNumber) {
            fskDemoState.status = fskDemodulationStatusFindFirst0Peak;
        }
        else {
            fskDemoState.status = fskDemodulationStatusFind1Peaks;
        }
        return fskDemoState.status;
    }

    // Thank god I found you
    if (fskDemoState.status == fskDemodulationStatusFirst0PeakFound) {
        if (fskDemoState.sampleId - fskDemoState.lastPeakId < fskDemoState.halfb) {
            return fskDemoState.status;
        }
        // distance from the last peak is too large, stop
        // TODO: Check tail?
        if ((fskDemoState.sampleId - fskDemoState.lastPeakId) > (fskDemoState.lenb +
        fskDemoState.halfb)) {
            fskDemoState.status = fskDemodulationStatusEnd;
// printf("end\n");
            return fskDemoState.status;
        }
        int testId = (fskDemoState.outEnjId + fskDemoConfig.maximumInterval / 2) %
        fskDemoConfig.maximumInterval;
        int isMax = isMaxima(fskDemoState.outEnj0, fskDemoConfig.maximumInterval, testId,
        fskDemoConfig.thresholdEnj0);
        if (isMax) {
// printf("found enj0: %f\n", (float) (fskDemoState.sampleId) / fskDemoConfig.samplingRate);
            fskDemoState.lastPeakId = fskDemoState.sampleId;
            if (fskDemoState.binaryLength < fskDemoConfig.maxBitNumber) {
               fskDemoState.binary[fskDemoState.binaryLength] = (fskDemoState.outEnj0[testId] / fskDemoState.outEnj1[testId] < fskDemoConfig.binaryThreshold);
               fskDemoState.binaryLength++;
            }
            return fskDemoState.status;
        }
        isMax = isMaxima(fskDemoState.outEnj1, fskDemoConfig.maximumInterval, testId,
        fskDemoConfig.thresholdEnj1);
        if (isMax) {
// printf("found enj1: %f\n", (float) (fskDemoState.sampleId) / fskDemoConfig.samplingRate);
            fskDemoState.lastPeakId = fskDemoState.sampleId;
            if (fskDemoState.binaryLength < fskDemoConfig.maxBitNumber) {
                fskDemoState.binary[fskDemoState.binaryLength] = (fskDemoState.outEnj0[testId] / fskDemoState.outEnj1[testId] < fskDemoConfig.binaryThreshold);
                fskDemoState.binaryLength++;
            }
            return fskDemoState.status;
        }
        return fskDemoState.status;
    }
    return fskDemoState.status;
}

FSKDemodulationStatus FSKDemodulation::processOne(float sample)
{
    return d->processOne(sample);
}

