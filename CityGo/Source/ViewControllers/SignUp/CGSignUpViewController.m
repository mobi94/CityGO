//
//  CGSignUpViewController.m
//  CityGo
//
//  Created by ruslan on 1/24/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGSignUpViewController.h"
#import "CGViewControllerProtected.h"
#import "GFSignUpTextField.h"
#import "GFRequiredSignUpTextField.h"
#import "UITextField+NextTextField.h"
#import "FDTakeController.h"
#import "CGSignUpProtocol.h"
#import <IQKeyboardManager/KeyboardManager.h>

@interface CGSignUpViewController ()<UITextFieldDelegate, UIPickerViewDelegate, UIPickerViewDataSource, UITextViewDelegate, UIAlertViewDelegate, UIScrollViewDelegate, FDTakeDelegate>

@property(weak, nonatomic) IBOutlet id<CGSignUpProtocol> authenticator;

@property (strong, nonatomic) IBOutletCollection(GFSignUpTextField) NSArray *signUpfields;

@property (strong, nonatomic) IBOutletCollection(GFSignUpTextField) NSArray *optionalFields;
@property (strong, nonatomic) IBOutletCollection(GFRequiredSignUpTextField) NSArray *requiredFields;

@property (weak, nonatomic) IBOutlet UIButton *profileImageButton;
@property (weak, nonatomic) IBOutlet UIScrollView *scrollView;
@property (weak, nonatomic) IBOutlet UIButton *dismissButton;
@property (weak, nonatomic) IBOutlet UIButton *signupButton;

@property(strong, nonatomic) IQKeyboardReturnKeyHandler *returnKeyHandler;

@property(strong, nonatomic) NSArray *genderDataSource;
@property(strong, nonatomic) FDTakeController *imagePicker;

@property(assign, nonatomic, getter = isUserInfoReady) BOOL userInfoReady;

- (IBAction)addAvatarButonClick:(id)sender;
- (IBAction)signupButtonClick:(id)sender;

- (void)setupDoneButton;
- (void)setupOptionalFields;
- (void)setupGenderDataSource;
- (void)setupTextFieldIdentifiers;
- (void)setupReturnKeyHandler;
- (void)setupImagePicker;

- (void)datePickerValueDidChange:(UIDatePicker *)datePicker;

- (void)gatherUserDataWithBlock:(void (^)(NSDictionary *))block;
- (void)signUp;

@end

@implementation CGSignUpViewController

#pragma mark -
#pragma mark Lifecycle

- (void)awakeFromNib
{
    [super awakeFromNib];
    
    [self setUserInfoReady:NO];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self setupImagePicker];
    [self setupReturnKeyHandler];
    [self setupDoneButton];
    [self setupOptionalFields];
    [self setupGenderDataSource];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.signupButton setEnabled:NO];
    
    [self registerObserver];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    [self setupTextFieldIdentifiers];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    [self hideHUD];
    
    [self unregisterObserver];
}

- (void)dealloc
{
    [self setReturnKeyHandler:nil];
}

#pragma mark -
#pragma mark Custom setup

- (void)setupImagePicker
{
    [self setImagePicker:[[FDTakeController alloc] init]];
    [_imagePicker setViewControllerForPresentingImagePickerController:self];
    [_imagePicker setDelegate:self];
    [_imagePicker setAllowsEditingPhoto:YES];
}

- (void)setupReturnKeyHandler
{
    [self setReturnKeyHandler:[[IQKeyboardReturnKeyHandler alloc] initWithViewController:self]];
    [_returnKeyHandler setDelegate:self];
}

- (void)setupDoneButton
{
    UIBarButtonItem *doneButton = [[UIBarButtonItem alloc] initWithTitle:@"Done"
                                                                   style:UIBarButtonItemStylePlain
                                                                  target:self
                                                                  action:@selector(signUp)];
    
    [doneButton setEnabled:_userInfoReady];
    
    [[self navigationItem] setRightBarButtonItem:doneButton];
}

- (void)setupOptionalFields
{
    UIDatePicker *datePicker = [UIDatePicker new];
    [datePicker setDatePickerMode:UIDatePickerModeDate];
    [datePicker addTarget:self
                   action:@selector(datePickerValueDidChange:)
         forControlEvents:UIControlEventValueChanged];
    
    UIPickerView *genderPicker = [UIPickerView new];
    [genderPicker setDelegate:self];
    [genderPicker setDataSource:self];
    
    NSPredicate *birthdayPredicate = [NSPredicate predicateWithFormat:@"%K CONTAINS %@", @"placeholder", @"Birthday"];
    NSPredicate *genderPredicate = [NSPredicate predicateWithFormat:@"%K CONTAINS %@", @"placeholder", @"Gender"];
    
    GFTextField *genderField = [[_optionalFields filteredArrayUsingPredicate:genderPredicate] firstObject];
    GFTextField *birthdayField = [[_optionalFields filteredArrayUsingPredicate:birthdayPredicate] firstObject];
    
    [genderField setInputView:genderPicker];
    [birthdayField setInputView:datePicker];
}

- (void)setupGenderDataSource
{
    [self setGenderDataSource:@[@"Male", @"Female"]];
}

- (void)setupTextFieldIdentifiers
{
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^
                   {
                       [_requiredFields makeObjectsPerformSelector:@selector(setupIdentifier)];
                       [_optionalFields makeObjectsPerformSelector:@selector(setupIdentifier)];
                   });
}

#pragma mark -
#pragma mark Actions

- (void)signUp
{
    [self showHUD];
    
    [self gatherUserDataWithBlock:^(NSDictionary *signUpData)
     {
         
     }];
}


- (IBAction)dismissButtonClick:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)addAvatarButonClick:(id)sender
{
    [_imagePicker takePhotoOrChooseFromLibrary];
}

- (IBAction)signupButtonClick:(id)sender
{
    
}

- (void)datePickerValueDidChange:(UIDatePicker *)datePicker
{
    NSDateFormatter *dateFormatter = [NSDateFormatter new];
    [dateFormatter setDateStyle:NSDateFormatterLongStyle];
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    
    NSString *dateString = [dateFormatter stringFromDate:[datePicker date]];
    
    NSPredicate *birthdayPredicate = [NSPredicate predicateWithFormat:@"%K CONTAINS %@", @"placeholder", @"Birthday"];
    GFTextField *birthdayField = [[_optionalFields filteredArrayUsingPredicate:birthdayPredicate] firstObject];
    
    [birthdayField setText:dateString];
}

#pragma mark -
#pragma mark UIPickerViewDataSource

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 1;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    return [_genderDataSource count];
}

#pragma mark -
#pragma mark UIPickerViewDelegate

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    return _genderDataSource[row];
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    NSPredicate *genderPredicate = [NSPredicate predicateWithFormat:@"%K CONTAINS %@", @"placeholder", @"Gender"];
    GFTextField *genderField = [[_optionalFields filteredArrayUsingPredicate:genderPredicate] firstObject];
    
    [genderField setText:_genderDataSource[row]];
}

#pragma mark -
#pragma mark UIITextFieldDelegate

- (void)textFieldDidBeginEditing:(UITextField *)textField
{
    [_scrollView setScrollEnabled:NO];
    
    if ([textField respondsToSelector:@selector(isValid)] && ![(GFRequiredSignUpTextField *)textField isValid])
    {
        [(GFRequiredSignUpTextField *)textField promptInContainerView:_scrollView];
    }
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    UITextPosition *begin = [textField beginningOfDocument];
    UITextPosition *start = [textField positionFromPosition:begin offset:range.location];
    UITextPosition *end = [textField positionFromPosition:start offset:range.length];
    UITextRange *txRange = [textField textRangeFromPosition:start toPosition:end];
    
    return [textField shouldChangeTextInRange:txRange replacementText:string];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if ([textField nextTextField])
    {
        [[textField nextTextField] becomeFirstResponder];
    }
    else
    {
        [textField resignFirstResponder];
    }
    
    return NO;
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    [_scrollView setScrollEnabled:YES];
    
    if ([textField respondsToSelector:@selector(validate)])
    {
        [(GFRequiredSignUpTextField *)textField finishEditing];
        [(GFRequiredSignUpTextField *)textField validate];
        
        if ([_requiredFields containsObject:textField])
        {
            NSPredicate *predicate = [NSPredicate predicateWithFormat:@"%K == %@", @"isValid", @NO];
            
            NSArray *notValidArray = [_requiredFields filteredArrayUsingPredicate:predicate];
            
            BOOL validityCheck = [notValidArray count] == 0;
            
            [self setUserInfoReady:validityCheck];
        }
    }
}

#pragma mark -
#pragma mark FDTakeDelegate

- (void)takeController:(FDTakeController *)controller gotPhoto:(UIImage *)photo withInfo:(NSDictionary *)info
{
    UIImage *selectedImage = info[UIImagePickerControllerEditedImage] ? : info[UIImagePickerControllerOriginalImage];
    
    if (!selectedImage)
    {
        return;
    }
    
    [_profileImageButton setImage:selectedImage forState:UIControlStateNormal];
}

#pragma mark -
#pragma mark Sign up data

- (void)gatherUserDataWithBlock:(void (^)(NSDictionary *))block
{
    NSArray *keys = [_signUpfields valueForKey:@"identifier"];
    NSArray *values = [_signUpfields valueForKey:@"text"];
    
    NSMutableDictionary *userData = [NSMutableDictionary dictionaryWithObjects:values forKeys:keys];
    
    if ([_profileImageButton imageForState:UIControlStateNormal])
    {
        NSDictionary *profileImageDict = @{@"photo" : [_profileImageButton imageForState:UIControlStateNormal]};
        
        [userData addEntriesFromDictionary:profileImageDict];
    }
    
    block(userData);
}

#pragma mark -
#pragma mark Done button management

- (void)setDoneButtonEnabled:(BOOL)enabled
{
    [self.signupButton setEnabled:enabled];
}

#pragma mark -
#pragma mark Observer

- (void)registerObserver
{
    typeof(self) __weak weakSelf = self;
    
    [self setNotificationsObserver:[NOTIFICATION_CENTER addObserverForName:UIKeyboardDidShowNotification
                                                                    object:nil
                                                                     queue:MAIN_QUEUE
                                                                usingBlock:^(NSNotification *note)
                                    {
                                        [weakSelf setDoneButtonEnabled:NO];
                                    }]];
    
    [self setNotificationsObserver:[NOTIFICATION_CENTER addObserverForName:UIKeyboardDidHideNotification
                                                                    object:nil
                                                                     queue:MAIN_QUEUE
                                                                usingBlock:^(NSNotification *note)
                                    {
                                        [weakSelf setDoneButtonEnabled:_userInfoReady];
                                    }]];
}


@end
