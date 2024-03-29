import Book from "../DTO/BookDTO";

type Props = {
    title: string;
    book: Book;
    handleOperation: () => void;
    setIsOperationActive: React.Dispatch<React.SetStateAction<boolean>>;
}

const Popup = (props: Props) => {

    const {title, book, handleOperation, setIsOperationActive} = {...props};

    const handleCancelOperation = () => {
        setIsOperationActive(false);
    }

    const handleSubmit = () => {
        handleOperation();
        handleCancelOperation();
    }

    return (  
        <div className="popup__background">
            <section className="popup__container">
                <h3 className="popup__title">{title.toUpperCase()} <span className="popup__paragraph"> - book of interest: </span></h3>
                <div className="popup__book">
                    <h4>{book.title}</h4>
                    <div className="book-description">
                        <p>Author: {book.author}</p>
                        <p>Publish year: {book.publishYear}</p>  
                        <p>Type: {book.type.name}</p>
                    </div>
                </div>
                <div className="popup__buttons">
                    <button className="btn btn--greater" onClick={handleCancelOperation}>cancel</button>
                    <button className="btn btn--greater btn--pink " onClick={handleSubmit}>{title}</button>
                </div>
            </section>
        </div>
    );
}
 
export default Popup;